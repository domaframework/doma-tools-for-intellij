insert into users (username, email)
values ('user', 'user@example.com')
on CONFLICT(username) do nothing
