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
)
SEARCH BREADTH FIRST BY emp_id SET order_seq
CYCLE emp_id SET is_cycle TO TRUE DEFAULT FALSE USING path_ids
SELECT * FROM org_tree;