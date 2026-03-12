from pathlib import Path
from unittest.mock import Mock

import pandas as pd
import pytest

SAMPLE_DATA_DIR = Path(__file__).parent.parent / "sample-data"


@pytest.fixture(scope="session")
def sample_data():
    """Load all sample data from CSV files."""
    return {
        "users": pd.read_csv(SAMPLE_DATA_DIR / "users.csv"),
        "posts": pd.read_csv(SAMPLE_DATA_DIR / "posts.csv"),
        "comments": pd.read_csv(SAMPLE_DATA_DIR / "comments.csv"),
    }


@pytest.fixture
def mock_db():
    """Mock database connection."""
    mock_conn = Mock()
    mock_cursor = Mock()
    mock_conn.cursor.return_value.__enter__.return_value = mock_cursor
    return mock_conn


@pytest.fixture
def sample_data_dir():
    """Path to sample data directory."""
    return str(SAMPLE_DATA_DIR)


@pytest.fixture
def users_config():
    return {
        "required_columns": {
            "users": ["id", "full_name", "email", "role", "created_at"],
        },
        "deduplicate_on": {"users": "id"},
        "timestamp_columns": {"users": "created_at"},
    }


@pytest.fixture
def dirty_users_data():
    return pd.DataFrame(
        [
            {
                "id": 1,
                "full_name": " John Doe 12 ",
                "email": " JOHN@EXAMPLE.COM ",
                "role": " Admin ",
                "created_at": "2026-01-01",
            },
            {
                "id": 1,
                "full_name": "John Doe 12",
                "email": "john@example.com",
                "role": "ADMIN",
                "created_at": "2026-01-01",
            },
            {
                "id": 2,
                "full_name": "Ama@@Mensah",
                "email": " ama@example.com ",
                "role": None,
                "created_at": None,
            },
            {
                "id": 3,
                "full_name": "12345",
                "email": " unknown@example.com ",
                "role": "",
                "created_at": "2026-01-03",
            },
        ]
    )
