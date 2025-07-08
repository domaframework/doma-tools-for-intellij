WITH RECURSIVE org_tree (emp_id , name, manager_id, level) AS  MATERIALIZED (
 SELECT id, name, manager_id, 1
      FROM employees
     WHERE manager_id IS NULL
 )
 search breadth first by emp_id set order_seq
 cycle emp_id SET is_cycle to TRUE default FALSE USING path_ids
SELECT * FROM org_tree