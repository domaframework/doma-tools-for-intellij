insert into employees (name, manager_id)
SELECT name, user_id
FROM user_settings
WHERE user_id = /*employee.id*/0  AND name = /*employee.name*/'name'
on conflict (id) do 
update set name = EXCLUDED.name
WHERE employees.name is Distinct from EXCLUDED.name
returning id, manager_id, name
