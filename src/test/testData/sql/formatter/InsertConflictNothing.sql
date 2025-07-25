insert into employee (id, username) values ( /* employees.id */0, /* employees.name */'name')
on conflict (username)  on Constraint do nothing
