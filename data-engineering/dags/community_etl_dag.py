from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime

from pipelines.etl_pipeline import run_pipeline

default_args = {
    "owner": "data-engineering",
    "start_date": datetime(2026, 1, 1),
    "retries": 1
}

with DAG(
    dag_id="community_etl_pipeline",
    default_args=default_args,
    schedule_interval="@hourly",
    catchup=False
) as dag:

    run_etl = PythonOperator(
        task_id="run_etl_pipeline",
        python_callable=run_pipeline
    )