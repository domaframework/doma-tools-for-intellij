insert into users (username, email)
values ('user', 'user@example.com')
on CONFLICT(username) ON constraint do update set email = EXCLUDED.email, created_at = CURRENT_TIMESTAMP 
