with recursive org_tree (emp_id, name, manager_id, level) AS materialized (
SELECT id, name, manager_id, 1
FROM employees
WHERE manager_id IS NULL
  union all
SELECT e.id, e.name, e.manager_id, ot.level + 1
FROM employees e
JOIN org_tree ot ON e.manager_id = ot.emp_id
) 
SELECT * FROM org_tree