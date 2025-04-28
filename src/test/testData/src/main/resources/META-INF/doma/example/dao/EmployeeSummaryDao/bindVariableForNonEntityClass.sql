-- Test referencing instance fields and methods of a non-Entity class
    SELECT
        e.employee_id
        , u.user_id
        , u.user_name
        , u.email
        , e.department
        , COUNT(pe.project_id)
        -- Field reference using an operator
        , /* employee.numberOfProjects * 10 */ AS projectPoint
    FROM user u
    JOIN employee e
        ON u.user_id = e.user_id
    LEFT JOIN project_employee pe
        ON e.employee_id = pe.employee_id
    WHERE
        -- Reference to a public field
        e.user_id = /* user.userId */0
        AND e.employee_id = /* employee.employeeId */0
        -- if statement using a private field
        /*%if employee.departmentId.startsWith("200") */
           AND e.department_id = /* employee.departmentId */'dept'
        /*%elseif employee.numberOfProjects >= 3 */
            AND pe.start_date <= CURRENT_DATE
            AND pe.end_date >= CURRENT_DATE
        /*%end*/
        -- Reference error for a non-existent field
        /*%for child : employee.<error descr="The field or method [projectIds] does not exist in the class [EmployeeSummary]">projectIds</error> */
           -- An error occurred because the referenced element was not correctly defined.
           AND pe.parent_project = /* <error descr="The bind variable [child] does not exist in the Dao method [bindVariableForNonEntityClass]">child</error>.projectId */0
           -- Reference error for a non-existent method
          AND pe.member_id IN /* employee.<error descr="The field or method [getTopProject] does not exist in the class [EmployeeSummary]">getTopProject</error>() */(0,1,2)
        /*%end */
    GROUP BY
        e.employee_id,
        u.user_id,
        u.user_name,
        u.email,
        e.department