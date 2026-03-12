from __future__ import annotations

from datetime import date, datetime
from typing import Any

from psycopg2 import sql

from loading.load_to_warehouse.warehouse_config import build_warehouse_load_settings
from utils.config_loader import load_config
from utils.database import get_connection


DateInput = date | datetime | str | None


def resolve_config(config: dict | None = None) -> dict:
    return config if config is not None else load_config()


def normalize_date_input(value: DateInput, field_name: str):
    if value is None:
        return None
    if isinstance(value, datetime):
        return value.date()
    if isinstance(value, date):
        return value
    if isinstance(value, str):
        try:
            return date.fromisoformat(value)
        except ValueError as exc:
            raise ValueError(
                f"Invalid ISO date for '{field_name}': {value!r}"
            ) from exc
    raise TypeError(f"Unsupported value for '{field_name}': {type(value).__name__}")


def get_kpi_view_reference(view_key: str, *, config: dict | None = None):
    resolved_config = resolve_config(config)
    settings = build_warehouse_load_settings(resolved_config)
    if not settings.kpis.enabled:
        raise RuntimeError("Warehouse KPI materialized views are disabled in config")
    if view_key not in settings.kpis.materialized_views:
        raise KeyError(f"Unknown KPI materialized view key: {view_key}")

    relation = sql.SQL("{}.{}").format(
        sql.Identifier(settings.schema_name),
        sql.Identifier(settings.kpis.materialized_views[view_key]),
    )
    return resolved_config, relation, settings


def build_activity_date_filter(
    start_date: DateInput = None,
    end_date: DateInput = None,
):
    normalized_start_date = normalize_date_input(start_date, "start_date")
    normalized_end_date = normalize_date_input(end_date, "end_date")

    if (
        normalized_start_date is not None
        and normalized_end_date is not None
        and normalized_start_date > normalized_end_date
    ):
        raise ValueError("'start_date' must be on or before 'end_date'")

    clauses = []
    params: list[object] = []
    if normalized_start_date is not None:
        clauses.append(sql.SQL("activity_date >= %s"))
        params.append(normalized_start_date)
    if normalized_end_date is not None:
        clauses.append(sql.SQL("activity_date <= %s"))
        params.append(normalized_end_date)

    if not clauses:
        return sql.SQL(""), params

    return sql.SQL("WHERE ") + sql.SQL(" AND ").join(clauses), params


def fetch_kpi_rows(
    query,
    params: list[object] | tuple[object, ...] | None = None,
    *,
    config: dict | None = None,
) -> list[dict[str, Any]]:
    resolved_config = resolve_config(config)
    settings = build_warehouse_load_settings(resolved_config)

    with get_connection(resolved_config, db_role=settings.db_role) as connection:
        with connection.cursor() as cursor:
            cursor.execute(query, params or ())
            columns = [column.name for column in cursor.description]
            return [
                dict(zip(columns, row, strict=True))
                for row in cursor.fetchall()
            ]
