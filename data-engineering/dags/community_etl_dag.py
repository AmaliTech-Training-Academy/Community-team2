from __future__ import annotations

from pathlib import Path
import sys

from airflow import DAG
from airflow.operators.empty import EmptyOperator
from airflow.operators.python import PythonOperator
from airflow.utils.context import Context
from airflow.utils.trigger_rule import TriggerRule


DAG_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = DAG_DIR.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from utils.airflow_runtime import emit_airflow_event, get_airflow_settings
from utils.logging import get_logger


logger = get_logger("community_etl_dag")
AIRFLOW_SETTINGS = get_airflow_settings()


def _handle_task_event(event_name: str, context: Context, *, result=None) -> None:
    emit_airflow_event(event_name, context, result=result)


def _log_task_failure(context: Context) -> None:
    _handle_task_event("failure", context)


def _log_task_retry(context: Context) -> None:
    _handle_task_event("retry", context)


def _log_task_success(context: Context) -> None:
    task_instance = context["task_instance"]
    result = task_instance.xcom_pull(task_ids=task_instance.task_id)
    _handle_task_event("success", context, result=result)


def _run_replica_sync_task() -> dict[str, int]:
    # Defer pipeline imports until task runtime so Airflow can parse the DAG quickly.
    from pipelines.etl_pipeline import run_replica_sync_pipeline

    result = run_replica_sync_pipeline()
    if AIRFLOW_SETTINGS.monitoring.enabled and AIRFLOW_SETTINGS.monitoring.log_result_summary:
        logger.info("Replica sync task completed: %s", result)
    return result


def _run_etl_task() -> dict:
    # Defer pipeline imports until task runtime so Airflow can parse the DAG quickly.
    from pipelines.etl_pipeline import run_etl_pipeline

    result = run_etl_pipeline()
    if AIRFLOW_SETTINGS.monitoring.enabled and AIRFLOW_SETTINGS.monitoring.log_result_summary:
        logger.info("ETL task completed: %s", result)
    return result


default_args = {
    "owner": AIRFLOW_SETTINGS.owner,
    "depends_on_past": AIRFLOW_SETTINGS.depends_on_past,
    "retries": AIRFLOW_SETTINGS.retries,
    "retry_delay": AIRFLOW_SETTINGS.retry_delay,
    "retry_exponential_backoff": AIRFLOW_SETTINGS.retry_exponential_backoff,
    "max_retry_delay": AIRFLOW_SETTINGS.max_retry_delay,
    "on_failure_callback": _log_task_failure,
    "on_retry_callback": _log_task_retry,
    "on_success_callback": _log_task_success,
}

with DAG(
    dag_id=AIRFLOW_SETTINGS.dag_id,
    description=AIRFLOW_SETTINGS.description,
    default_args=default_args,
    start_date=AIRFLOW_SETTINGS.start_date,
    schedule_interval=AIRFLOW_SETTINGS.schedule,
    catchup=AIRFLOW_SETTINGS.catchup,
    max_active_runs=AIRFLOW_SETTINGS.max_active_runs,
    dagrun_timeout=AIRFLOW_SETTINGS.dagrun_timeout,
    tags=list(AIRFLOW_SETTINGS.tags),
    render_template_as_native_obj=AIRFLOW_SETTINGS.render_template_as_native_obj,
    doc_md="""
    ### Community ETL Pipeline

    This DAG orchestrates the production data flow in two explicit steps:
    1. Sync raw backend data into the replica/staging database.
    2. Transform replica data and load the analytics warehouse.
    """,
) as dag:
    start = EmptyOperator(task_id="start")

    replica_sync = PythonOperator(
        task_id=AIRFLOW_SETTINGS.replica_sync_task.task_id,
        python_callable=_run_replica_sync_task,
        execution_timeout=AIRFLOW_SETTINGS.replica_sync_task.execution_timeout,
    )

    etl_load = PythonOperator(
        task_id=AIRFLOW_SETTINGS.etl_load_task.task_id,
        python_callable=_run_etl_task,
        execution_timeout=AIRFLOW_SETTINGS.etl_load_task.execution_timeout,
    )

    finish = EmptyOperator(
        task_id="finish",
        trigger_rule=TriggerRule.NONE_FAILED,
    )

    start >> replica_sync >> etl_load >> finish
