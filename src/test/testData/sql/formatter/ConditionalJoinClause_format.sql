SELECT e.*
       , d.department_name
       , p.project_name
  FROM employees e
       /*%if includeDepartments */
       INNER JOIN departments d
               ON e.department_id = d.id
       /*%end*/
       /*%if includeProjects */
       LEFT JOIN projects p
              ON e.project_id = p.id
       /*%elseif includeActiveProjects */
       INNER JOIN projects p
               ON e.project_id = p.id
              AND p.status = 'active'
       /*%end*/
       /*%if includeSkills */
       LEFT JOIN employee_skills es
              ON e.id = es.employee_id
       INNER JOIN skills s
               ON es.skill_id = s.id
       /*%end*/
 WHERE e.active = true
