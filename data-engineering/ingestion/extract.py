def extract_data(source):

    users = source.get_users()
    posts = source.get_posts()
    comments = source.get_comments()

    return users, posts, comments