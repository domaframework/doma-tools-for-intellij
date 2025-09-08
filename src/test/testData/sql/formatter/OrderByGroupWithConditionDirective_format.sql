SELECT e.id
       -- with condition
       , ROW_NUMBER()
         /*%if order */
         OVER(ORDER BY e.manager_id DESC)
         /*%end*/
         AS row_num
       , DENSE_RANK() OVER(PARTITION BY department_id
                           ORDER BY e.id ASC, e.manager_id ASC, created_at DESC
                           /*%if rows */
                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
                           /*%end */) AS dense_rank_num
       , SUM(amount)
         /*%if filter */
         FILTER(WHERE status = 'active')
         /*%end*/
         AS dept_salary_avg
       , COUNT(*) OVER(ORDER BY e.id, e.manager_id, created_at
                       /*%if rows */
                       ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING
                       /*%end*/)
       , COUNT(*) FILTER(WHERE gender = 'F'
                           AND id > 10) AS female_count
       , FIRST_VALUE(salary) IGNORE NULLS OVER(PARTITION BY department_id
                                               ORDER BY e.id ASC, e.manager_id ASC, created_at DESC)
       , LAST_VALUE() OVER(ORDER BY e.manager_id ASC
                           /*%if rows */
                           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
                           /*%end*/) AS bottom_salary
       , LISTAGG(e.name
                 , ', ')
         /*%if filter */
         WITHIN GROUP (ORDER BY name DESC)
         /*%end*/
  FROM employees e
 WHERE e.status = 1
 ORDER BY e.id
          , e
          , manager_id
