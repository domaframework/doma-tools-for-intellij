INSERT INTO employee_project (employee_name, department, project)
         (
             SELECT
                 e1.employee_name,
                 e1.department,
                 p1.project
             FROM
                 employee e1
                 JOIN user u1 ON e1.employee_id = u1.user_id
                              AND e1.user_tag = /*# 't' */'a'
                              AND e1.user_dept = /*# "development" */'dev'
                  -- Access to parent private field
                   WHERE u1.user_name = /* employee.userName.toLowerCase() */'name'
                   -- Access to non-existent parent field
                   OR u1.user_name = /* employee.<error descr="The field or method [userFirstName] does not exist in the class [Employee]">userFirstName</error>.toLowerCase() */'name'
                   -- Public parent method
                   OR u1.user_name = /* employee.getUserNameFormat() */'name'
                   -- Private parent method
                   OR u1.email = /* employee.<error descr="The field or method [getEmail] does not exist in the class [Employee]">getEmail</error>() */'email'
                 JOIN project p1 ON e1.employee_id = p1.employee_id
                   WHERE
                   -- Public entity method
                   p1.project_id = /* employee.getFirstProject().projectId */0
                   -- Private entity method
                   AND p1.base_rank >= /* employee.<error descr="The field or method [getEmployeeRank] does not exist in the class [Employee]">getEmployeeRank</error>() */0
             WHERE
             -- Access to private field of entity class
                 e1.employee_id = /* employee.employeeId */0
             /*%for project : employee.projects */
             UNION ALL
             SELECT
                 e2.employee_name,
                 e2.department,
                 -- Use of for item
                 /* project */'project'
             FROM
                 employee e2
                 JOIN user u2 ON e2.employee_id = u2.user_id
                 -- Access to parent public field
                 AND u2.user_id = /* employee.userId */0
             WHERE
                 -- Access to public field of entity class
                 e2.employee_id = /* employee.employeeId */0
                 -- Access to private field of entity class
                 AND e2.department = /* employee.department */'department'
                 -- Access to non-existent field of entity class
                 OR  AND e2.department = /* employee.<error descr="The field or method [subDepartment] does not exist in the class [Employee]">subDepartment</error> */'department'
                 -- Access to non-existent method of entity class
                 OR  AND e2.department = /* employee.<error descr="The field or method [getFirstDepartment] does not exist in the class [Employee]">getFirstDepartment</error>() */'department'
             /*%end*/
         )