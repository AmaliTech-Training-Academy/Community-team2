from utils.config_loader import load_config
from sources.csv_source import CSVSource
from sources.postgres_source import PostgresSource

def run_pipeline():

    config = load_config()

    if config["data_source"] == "csv":
        source = CSVSource(config)

    elif config["data_source"] == "postgres":
        source = PostgresSource(config)

    users, posts, comments = source.get_users(), source.get_posts(), source.get_comments()

    print("Pipeline executed")