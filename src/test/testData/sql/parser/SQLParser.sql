-- Psi tree comparison test data for keywords, directives, bind variables, member accesses, and static property calls
SELECT
    e.employee_id
    , u.user_id
    , u.user_name
    , u.email
    , e.department
    , COUNT(pe.project_id)
    , /* employee.numberOfProjects * 10 */ AS projectPoint
FROM user u
JOIN employee e
    ON u.user_id = e.user_id
LEFT JOIN project_employee pe
    ON e.employee_id = pe.employee_id
WHERE
    e.user_id = /* user.userId */0
    AND e.employee_id = /* employee.employeeId */0
    AND e.join_date <= /* referenceDate */'2099/12/31'
/*%if @isNotBlank(employee.departmentId) */
    /*%if employee.departmentId.startsWith("200") */
      　AND e.department_id = /* employee.departmentId */'dept'
    /*%elseif employee.numberOfProjects >= 3 */
        AND pe.start_date <= CURRENT_DATE
        AND pe.end_date >= CURRENT_DATE
    /*%end*/
    /*%for child : employee.departmentId */
       /*%if  child_has_next */
       AND pe.parent_project = /* child.projectId */0
      /*%for p : child.member */
      AND pe.type = /* @example.entity.StaticType@PARAM1.getValue() */'0'
      /*%end */
      /*%end */
    /*%end */
      /*%end */
GROUP BY
    e.employee_id,
    u.user_id,
    u.user_name,
    u.email,
    e.department