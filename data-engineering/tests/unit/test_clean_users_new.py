import pandas as pd
import pytest

from transformations.clean_users import clean_users


class TestUserTransformations:

    @pytest.mark.unit
    def test_sanitizes_unrealistic_names(self, dirty_users_data, users_config):
        cleaned = clean_users(dirty_users_data, users_config, "users")

        assert "John Doe" in cleaned["full_name"].values
        assert "AmaMensah" in cleaned["full_name"].values
        assert "Unknown User" in cleaned["full_name"].values
        assert "John Doe 12" not in cleaned["full_name"].values
        assert "12345" not in cleaned["full_name"].values

    @pytest.mark.unit
    def test_removes_duplicates_by_id(self, dirty_users_data, users_config):
        cleaned = clean_users(dirty_users_data, users_config, "users")

        assert cleaned["id"].is_unique
        assert len(cleaned) == 3

    @pytest.mark.unit
    def test_normalizes_emails(self, dirty_users_data, users_config):
        cleaned = clean_users(dirty_users_data, users_config, "users")

        assert cleaned.loc[cleaned["id"] == 1, "email"].iloc[0] == "john@example.com"
        assert cleaned.loc[cleaned["id"] == 2, "email"].iloc[0] == "ama@example.com"

    @pytest.mark.unit
    def test_normalizes_roles(self, dirty_users_data, users_config):
        cleaned = clean_users(dirty_users_data, users_config, "users")

        assert cleaned.loc[cleaned["id"] == 1, "role"].iloc[0] == "admin"
        assert cleaned.loc[cleaned["id"] == 2, "role"].iloc[0] == "unknown"
        assert cleaned.loc[cleaned["id"] == 3, "role"].iloc[0] == "unknown"

    @pytest.mark.unit
    def test_fills_missing_timestamps(self, dirty_users_data, users_config):
        cleaned = clean_users(dirty_users_data, users_config, "users")

        assert cleaned["created_at"].notna().all()
        assert pd.api.types.is_datetime64_any_dtype(cleaned["created_at"])
