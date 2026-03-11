import pandas as pd
import random
from faker import Faker
from utils.config_loader import load_config
from utils.logging import get_logger
from utils.settings import OUTPUT_DIR


# -------------------------
# Logging Configuration
# -------------------------

logger = get_logger("generate_sample_data")


# -------------------------
# Reproducibility
# -------------------------

Faker.seed(42)
random.seed(42)

fake = Faker()


# -------------------------
# Load Configuration
# -------------------------

try:
    config = load_config()

    NUM_USERS = config["sample_data"]["users"]
    NUM_POSTS = config["sample_data"]["posts"]
    NUM_COMMENTS = config["sample_data"]["comments"]

    CATEGORIES = config["categories"]

    # 30% of users generate most of the posts
    ACTIVE_USERS_RATIO = config["sample_data"]["active_users_ratio"]

except Exception as e:
    logger.error("Failed to load configuration.")
    raise e



# -------------------------
# Helper Functions
# -------------------------

def generate_post_time():
    """
    Generate realistic post timestamps during active hours.
    """
    dt = fake.date_time_between(start_date="-30d", end_date="now")
    return dt.replace(hour=random.randint(8, 21))


# -------------------------
# Data Generators
# -------------------------

def generate_users(n: int) -> pd.DataFrame:
    """
    Generate sample users dataset.
    """

    try:

        logger.info(f"Generating {n} users")

        users = []

        for i in range(1, n + 1):
            users.append({
                "id": i,
                "username": fake.user_name(),
                "email": fake.email(),
                "role": random.choice(["member", "admin"]),
                "created_at": fake.date_time_this_year()
            })

        df = pd.DataFrame(users)

        logger.info("Users dataset generated successfully")

        return df

    except Exception as e:
        logger.exception("Error generating users dataset")
        raise e


def generate_posts(n: int, users_df: pd.DataFrame) -> pd.DataFrame:
    """
    Generate sample posts dataset with active-user bias.
    """

    try:

        logger.info(f"Generating {n} posts")

        posts = []

        user_ids = users_df["id"].tolist()

        active_users = random.sample(
            user_ids,
            max(1, int(len(user_ids) * ACTIVE_USERS_RATIO))
        )

        def pick_user():
            if random.random() < 0.7:
                return random.choice(active_users)
            return random.choice(user_ids)

        for i in range(1, n + 1):
            posts.append({
                "id": i,
                "user_id": pick_user(),
                "title": fake.sentence(nb_words=6),
                "content": fake.text(max_nb_chars=200),
                "category": random.choice(CATEGORIES),
                "created_at": generate_post_time()
            })

        df = pd.DataFrame(posts)

        logger.info("Posts dataset generated successfully")

        return df

    except Exception as e:
        logger.exception("Error generating posts dataset")
        raise e


def generate_comments(posts_df: pd.DataFrame, users_df: pd.DataFrame) -> pd.DataFrame:
    """
    Generate realistic comments dataset where comments occur after posts
    and some posts receive higher engagement.
    """

    try:

        logger.info(f"Generating up to {NUM_COMMENTS} comments")

        comments = []

        user_ids = users_df["id"].tolist()

        comment_id = 1

        for post in posts_df.itertuples():

            if random.random() < 0.2:
                n_comments = random.randint(10, 20)
            else:
                n_comments = random.randint(0, 5)

            for _ in range(n_comments):

                if comment_id > NUM_COMMENTS:
                    break

                comment_time = fake.date_time_between(
                    start_date=post.created_at,
                    end_date="+2d"
                )

                comments.append({
                    "id": comment_id,
                    "post_id": post.id,
                    "user_id": random.choice(user_ids),
                    "content": fake.sentence(),
                    "created_at": comment_time
                })

                comment_id += 1

            if comment_id > NUM_COMMENTS:
                break

        df = pd.DataFrame(comments)

        logger.info("Comments dataset generated successfully")

        return df

    except Exception as e:
        logger.exception("Error generating comments dataset")
        raise e


# -------------------------
# Main Pipeline
# -------------------------

def main():
    """
    Generate and save realistic sample datasets for development.
    """

    try:

        logger.info("Starting sample data generation")

        users = generate_users(NUM_USERS)
        posts = generate_posts(NUM_POSTS, users)
        comments = generate_comments(posts, users)

        users.to_csv(OUTPUT_DIR / "users.csv", index=False)
        posts.to_csv(OUTPUT_DIR / "posts.csv", index=False)
        comments.to_csv(OUTPUT_DIR / "comments.csv", index=False)

        logger.info("Sample data generation completed successfully")
        logger.info(f"Users generated: {len(users)}")
        logger.info(f"Posts generated: {len(posts)}")
        logger.info(f"Comments generated: {len(comments)}")

    except Exception as e:
        logger.exception("Sample data generation failed")
        raise e


if __name__ == "__main__":
    main()
