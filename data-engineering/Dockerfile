FROM apache/airflow:2.9.3-python3.11

USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential libpq-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER airflow
WORKDIR /opt/airflow/data-engineering

COPY --chown=airflow:root requirements.txt /tmp/requirements.txt
RUN python - <<'PY'
from pathlib import Path
src = Path('/tmp/requirements.txt')
lines = [line for line in src.read_text(encoding='utf-8').splitlines() if line.strip() and not line.strip().startswith('apache-airflow')]
Path('/tmp/project-requirements.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
PY
RUN if [ -s /tmp/project-requirements.txt ]; then pip install --no-cache-dir -r /tmp/project-requirements.txt; fi

COPY --chown=airflow:root . /opt/airflow/data-engineering

ENV PYTHONPATH=/opt/airflow/data-engineering \
    AIRFLOW__CORE__DAGS_FOLDER=/opt/airflow/data-engineering/dags
