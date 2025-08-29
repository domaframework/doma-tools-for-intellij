SELECT e.id
       , e.name
       , ROW_NUMBER() OVER(ORDER BY e.manager_id DESC) AS row_num
       , RANK() OVER(PARTITION BY department_id
                     ORDER BY e.manager_id DESC
                     ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS rank_num
       , DENSE_RANK() OVER(PARTITION BY department_id
                           ORDER BY e.id ASC, e.manager_id ASC, created_at DESC
                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS dense_rank_num
       , SUM(amount) FILTER(WHERE status = 'active') AS dept_salary_avg
       , COUNT(*) OVER(ORDER BY e.id, e.manager_id, created_at
                       ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING)
       , FIRST_VALUE(salary) IGNORE NULLS OVER(PARTITION BY department_id
                                               ORDER BY e.id ASC, e.manager_id ASC, created_at DESC)
       , LAST_VALUE() OVER(ORDER BY e.manager_id ASC
                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS bottom_salary
       , COUNT(*) FILTER(WHERE gender = 'F') AS female_count
       , COUNT(*) FILTER(WHERE gender = 'F'
                           AND id > 10) AS female_count
       , LISTAGG(e.name
                 , ', ') WITHIN GROUP (ORDER BY name DESC)
  FROM employees e
 WHERE e.status = 1
 ORDER BY e.id
          , e
          , manager_id
