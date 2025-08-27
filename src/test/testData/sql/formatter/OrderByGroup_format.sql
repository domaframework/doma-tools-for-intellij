SELECT e.id
       , e.name
       , ROW_NUMBER() OVER(ORDER BY e.manager_id DESC) AS row_num
       , RANK() OVER(ORDER BY e.manager_id DESC) AS rank_num
       , DENSE_RANK() OVER(ORDER BY e.manager_id DESC) AS dense_rank_num
       , SUM(e.manager_id) OVER(PARTITION BY e.id ORDER BY e.manager_id DESC) AS dept_salary_sum
       , AVG(e.manager_id) OVER(PARTITION BY e.id) AS dept_salary_avg
       , COUNT(*) OVER(PARTITION BY e.id) AS dept_count
       , FIRST_VALUE(e.manager_id) OVER(ORDER BY e.manager_id DESC) AS top_salary
       , LAST_VALUE(e.manager_id) OVER(ORDER BY e.manager_id ASC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS bottom_salary
       , LEAD(e.manager_id) OVER(ORDER BY e.manager_id DESC) AS next_salary
       , LAG(e.manager_id) OVER(ORDER BY e.manager_id DESC) AS prev_salary
  FROM employees e
 WHERE e.status = 1
 ORDER BY e.id DESC
          , e.name ASC
