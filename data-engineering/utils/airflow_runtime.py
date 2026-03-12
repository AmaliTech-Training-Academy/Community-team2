from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
import json
from typing import Any
from urllib import request

from utils.config_loader import load_config
from utils.logging import get_logger


logger = get_logger("airflow_runtime")
TRUE_VALUES = {"1", "true", "yes", "on"}
FALSE_VALUES = {"0", "false", "no", "off", ""}


@dataclass(frozen=True)
class AirflowTaskSettings:
    task_id: str
    execution_timeout: timedelta


@dataclass(frozen=True)
class AirflowNotificationChannel:
    enabled: bool
    on_failure: bool
    on_retry: bool
    on_success: bool


@dataclass(frozen=True)
class EmailNotificationSettings(AirflowNotificationChannel):
    recipients: tuple[str, ...]
    subject_prefix: str


@dataclass(frozen=True)
class WebhookNotificationSettings(AirflowNotificationChannel):
    url: str


@dataclass(frozen=True)
class AirflowMonitoringSettings:
    enabled: bool
    log_task_events: bool
    log_result_summary: bool


@dataclass(frozen=True)
class AirflowDagSettings:
    dag_id: str
    description: str
    start_date: datetime
    schedule: str
    catchup: bool
    max_active_runs: int
    dagrun_timeout: timedelta
    render_template_as_native_obj: bool
    tags: tuple[str, ...]
    owner: str
    depends_on_past: bool
    retries: int
    retry_delay: timedelta
    retry_exponential_backoff: bool
    max_retry_delay: timedelta
    replica_sync_task: AirflowTaskSettings
    etl_load_task: AirflowTaskSettings
    monitoring: AirflowMonitoringSettings
    email_notifications: EmailNotificationSettings
    webhook_notifications: WebhookNotificationSettings


def _require_mapping(parent: dict, key: str, path: str) -> dict:
    value = parent.get(key)
    if not isinstance(value, dict):
        raise KeyError(f"Config is missing '{path}'")
    return value


def _coerce_bool(value, path: str) -> bool:
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        normalized_value = value.strip().lower()
        if normalized_value in TRUE_VALUES:
            return True
        if normalized_value in FALSE_VALUES:
            return False
    raise ValueError(f"Config value '{path}' must be a boolean")


def _require_bool(parent: dict, key: str, path: str) -> bool:
    return _coerce_bool(parent.get(key), path)


def _require_int(parent: dict, key: str, path: str, *, minimum: int | None = None) -> int:
    value = parent.get(key)
    if isinstance(value, str) and value.strip().isdigit():
        value = int(value.strip())
    if not isinstance(value, int):
        raise ValueError(f"Config value '{path}' must be an integer")
    if minimum is not None and value < minimum:
        raise ValueError(f"Config value '{path}' must be >= {minimum}")
    return value


def _require_str(parent: dict, key: str, path: str) -> str:
    value = parent.get(key)
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"Config value '{path}' must be a non-empty string")
    return value.strip()


def _parse_recipients(value, path: str) -> tuple[str, ...]:
    if isinstance(value, list):
        return tuple(recipient.strip() for recipient in value if isinstance(recipient, str) and recipient.strip())
    if isinstance(value, str):
        return tuple(recipient.strip() for recipient in value.split(",") if recipient.strip())
    raise ValueError(f"Config value '{path}' must be a list or comma-separated string")


def _parse_minutes(parent: dict, key: str, path: str, *, minimum: int = 1) -> timedelta:
    return timedelta(minutes=_require_int(parent, key, path, minimum=minimum))


def _parse_start_date(start_date_value: Any) -> datetime:
    if not isinstance(start_date_value, str) or not start_date_value.strip():
        raise ValueError("Config value 'airflow.dag.start_date' must be a non-empty ISO timestamp")

    normalized_value = start_date_value.strip().replace("Z", "+00:00")
    parsed_value = datetime.fromisoformat(normalized_value)
    if parsed_value.tzinfo is None:
        parsed_value = parsed_value.replace(tzinfo=timezone.utc)
    return parsed_value.astimezone(timezone.utc)


def _build_task_settings(tasks_settings: dict, task_key: str) -> AirflowTaskSettings:
    task_settings = _require_mapping(tasks_settings, task_key, f"airflow.tasks.{task_key}")
    return AirflowTaskSettings(
        task_id=_require_str(task_settings, "task_id", f"airflow.tasks.{task_key}.task_id"),
        execution_timeout=_parse_minutes(
            task_settings,
            "execution_timeout_minutes",
            f"airflow.tasks.{task_key}.execution_timeout_minutes",
        ),
    )


def _build_email_settings(notifications: dict) -> EmailNotificationSettings:
    email_settings = _require_mapping(notifications, "email", "airflow.notifications.email")

    return EmailNotificationSettings(
        enabled=_require_bool(email_settings, "enabled", "airflow.notifications.email.enabled"),
        on_failure=_require_bool(email_settings, "on_failure", "airflow.notifications.email.on_failure"),
        on_retry=_require_bool(email_settings, "on_retry", "airflow.notifications.email.on_retry"),
        on_success=_require_bool(email_settings, "on_success", "airflow.notifications.email.on_success"),
        recipients=_parse_recipients(
            email_settings.get("to", []),
            "airflow.notifications.email.to",
        ),
        subject_prefix=_require_str(
            email_settings,
            "subject_prefix",
            "airflow.notifications.email.subject_prefix",
        ),
    )


def _build_webhook_settings(notifications: dict) -> WebhookNotificationSettings:
    webhook_settings = _require_mapping(notifications, "webhook", "airflow.notifications.webhook")
    url = webhook_settings.get("url", "")
    if url is None:
        url = ""
    if not isinstance(url, str):
        raise ValueError("Config value 'airflow.notifications.webhook.url' must be a string")

    return WebhookNotificationSettings(
        enabled=_require_bool(webhook_settings, "enabled", "airflow.notifications.webhook.enabled"),
        on_failure=_require_bool(webhook_settings, "on_failure", "airflow.notifications.webhook.on_failure"),
        on_retry=_require_bool(webhook_settings, "on_retry", "airflow.notifications.webhook.on_retry"),
        on_success=_require_bool(webhook_settings, "on_success", "airflow.notifications.webhook.on_success"),
        url=url.strip(),
    )


def get_airflow_settings(config: dict | None = None) -> AirflowDagSettings:
    resolved_config = config or load_config()
    airflow_settings = _require_mapping(resolved_config, "airflow", "airflow")
    dag_settings = _require_mapping(airflow_settings, "dag", "airflow.dag")
    default_args = _require_mapping(airflow_settings, "default_args", "airflow.default_args")
    tasks_settings = _require_mapping(airflow_settings, "tasks", "airflow.tasks")
    monitoring_settings = _require_mapping(airflow_settings, "monitoring", "airflow.monitoring")
    notifications = _require_mapping(airflow_settings, "notifications", "airflow.notifications")

    tags = dag_settings.get("tags", [])
    if not isinstance(tags, list):
        raise ValueError("Config value 'airflow.dag.tags' must be a list")

    return AirflowDagSettings(
        dag_id=_require_str(dag_settings, "id", "airflow.dag.id"),
        description=_require_str(dag_settings, "description", "airflow.dag.description"),
        start_date=_parse_start_date(dag_settings.get("start_date")),
        schedule=_require_str(dag_settings, "schedule", "airflow.dag.schedule"),
        catchup=_require_bool(dag_settings, "catchup", "airflow.dag.catchup"),
        max_active_runs=_require_int(dag_settings, "max_active_runs", "airflow.dag.max_active_runs", minimum=1),
        dagrun_timeout=_parse_minutes(dag_settings, "dagrun_timeout_minutes", "airflow.dag.dagrun_timeout_minutes"),
        render_template_as_native_obj=_require_bool(
            dag_settings,
            "render_template_as_native_obj",
            "airflow.dag.render_template_as_native_obj",
        ),
        tags=tuple(tag for tag in tags if isinstance(tag, str) and tag.strip()),
        owner=_require_str(default_args, "owner", "airflow.default_args.owner"),
        depends_on_past=_require_bool(default_args, "depends_on_past", "airflow.default_args.depends_on_past"),
        retries=_require_int(default_args, "retries", "airflow.default_args.retries", minimum=0),
        retry_delay=_parse_minutes(default_args, "retry_delay_minutes", "airflow.default_args.retry_delay_minutes"),
        retry_exponential_backoff=_require_bool(
            default_args,
            "retry_exponential_backoff",
            "airflow.default_args.retry_exponential_backoff",
        ),
        max_retry_delay=_parse_minutes(
            default_args,
            "max_retry_delay_minutes",
            "airflow.default_args.max_retry_delay_minutes",
        ),
        replica_sync_task=_build_task_settings(tasks_settings, "replica_sync"),
        etl_load_task=_build_task_settings(tasks_settings, "etl_load"),
        monitoring=AirflowMonitoringSettings(
            enabled=_require_bool(monitoring_settings, "enabled", "airflow.monitoring.enabled"),
            log_task_events=_require_bool(monitoring_settings, "log_task_events", "airflow.monitoring.log_task_events"),
            log_result_summary=_require_bool(monitoring_settings, "log_result_summary", "airflow.monitoring.log_result_summary"),
        ),
        email_notifications=_build_email_settings(notifications),
        webhook_notifications=_build_webhook_settings(notifications),
    )


def _should_notify(channel: AirflowNotificationChannel, event_name: str) -> bool:
    return (
        (event_name == "failure" and channel.on_failure)
        or (event_name == "retry" and channel.on_retry)
        or (event_name == "success" and channel.on_success)
    )


def _build_notification_message(event_name: str, context, result: Any = None) -> tuple[str, str, dict[str, Any]]:
    task_instance = context["task_instance"]
    payload = {
        "event": event_name,
        "dag_id": task_instance.dag_id,
        "task_id": task_instance.task_id,
        "run_id": context.get("run_id"),
        "try_number": task_instance.try_number,
        "logical_date": str(context.get("logical_date")),
        "exception": str(context.get("exception")) if context.get("exception") else None,
        "result": result,
    }
    subject = f"{task_instance.dag_id}.{task_instance.task_id} {event_name}"
    body = json.dumps(payload, indent=2, default=str)
    return subject, body, payload


def _send_email_notification(settings: AirflowDagSettings, subject: str, body: str, event_name: str) -> None:
    email_settings = settings.email_notifications
    if not email_settings.enabled or not email_settings.recipients or not _should_notify(email_settings, event_name):
        return

    from airflow.utils.email import send_email

    send_email(
        to=list(email_settings.recipients),
        subject=f"{email_settings.subject_prefix} {subject}",
        html_content=f"<pre>{body}</pre>",
    )


def _send_webhook_notification(settings: AirflowDagSettings, payload: dict[str, Any], event_name: str) -> None:
    webhook_settings = settings.webhook_notifications
    if not webhook_settings.enabled or not webhook_settings.url or not _should_notify(webhook_settings, event_name):
        return

    data = json.dumps(payload).encode("utf-8")
    request.urlopen(
        request.Request(
            webhook_settings.url,
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        ),
        timeout=10,
    ).read()


def emit_airflow_event(event_name: str, context, *, result: Any = None) -> None:
    settings = get_airflow_settings()
    subject, body, payload = _build_notification_message(event_name, context, result=result)

    if settings.monitoring.enabled and settings.monitoring.log_task_events:
        log_fn = logger.info if event_name == "success" else logger.warning if event_name == "retry" else logger.error
        log_fn("Airflow task %s | %s", event_name, body)

    try:
        _send_email_notification(settings, subject, body, event_name)
    except Exception:
        logger.exception("Failed to send Airflow email notification for %s", event_name)

    try:
        _send_webhook_notification(settings, payload, event_name)
    except Exception:
        logger.exception("Failed to send Airflow webhook notification for %s", event_name)
