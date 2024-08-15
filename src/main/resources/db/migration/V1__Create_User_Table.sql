CREATE TABLE IF NOT EXISTS user_table (
    id SERIAL PRIMARY KEY,
    user_name TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL
)