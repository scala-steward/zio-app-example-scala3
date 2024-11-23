CREATE TABLE IF NOT EXISTS user_delete_table(
    user_id INT NOT NULL,
    deletion_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_table(id),
    UNIQUE(user_id)
);