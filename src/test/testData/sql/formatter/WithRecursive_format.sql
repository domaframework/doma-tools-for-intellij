WITH RECURSIVE org_tree (emp_id
                         , name
                         , manager_id
                         , level) AS MATERIALIZED (
    SELECT id
           , name
           , manager_id
           , 1
      FROM employees
     WHERE manager_id IS NULL
    UNION ALL
    SELECT e.id
           , e.name
           , e.manager_id
           , ot.level + 1
      FROM employees e
           JOIN org_tree ot
             ON e.manager_id = ot.emp_id
)
SELECT *
  FROM org_tree
