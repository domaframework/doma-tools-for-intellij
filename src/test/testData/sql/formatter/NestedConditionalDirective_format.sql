SELECT e.*
       , d.department_name
       , s.salary_amount
  FROM employees e
       /*%if departmentFilter != null */
       INNER JOIN departments d
               ON e.department_id = d.id
       /*%end*/
       /*%if includeSalary */
       LEFT JOIN salaries s
              ON e.id = s.employee_id
             /*%if salaryFilter != null */
             AND s.effective_date = ( SELECT MAX(s2.effective_date)
                     FROM salaries s2
                    WHERE s2.employee_id = e.id
                      /*%if salaryFilter.maxDate != null */
                      AND s2.effective_date <= /* salaryFilter.maxDate */'2024-01-01'
                      /*%end*/)
             /*%end*/
       /*%end*/
 WHERE
       /*%if departmentFilter != null */
         /*%if departmentFilter.includeActive */
         d.is_active = true
           /*%if departmentFilter.locations != null && departmentFilter.locations.size() > 0 */
           AND d.location IN /* departmentFilter.locations */('Tokyo', 'Osaka')
           /*%end*/
         /*%else*/
         d.is_active = false
         /*%end*/
         /*%if employeeFilter != null */
         AND
             /*%if employeeFilter.minExperience != null */
             e.experience_years >= /* employeeFilter.minExperience */5
               /*%if employeeFilter.maxExperience != null */
               AND e.experience_years <= /* employeeFilter.maxExperience */10
               /*%end*/
             /*%end*/
           /*%if employeeFilter.skills != null && employeeFilter.skills.size() > 0 */
           AND EXISTS
                      ( SELECT 1
                          FROM employee_skills es
                         WHERE es.employee_id = e.id
                           /*%if employeeFilter.skillMatchType == "all" */
                           AND es.skill_id IN /* employeeFilter.skills */(1, 2, 3)
                         GROUP BY es.employee_id
                        HAVING COUNT(DISTINCT es.skill_id) = /* employeeFilter.skills.size() */3
                           /*%else*/
                           AND es.skill_id IN /* employeeFilter.skills */(1, 2, 3)
                           /*%end*/)
           /*%end*/
         /*%end*/
       /*%else*/
       e.department_id IS NULL
       /*%end*/
 ORDER BY e.created_at DESC
