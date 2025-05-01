INSERT INTO employee
            (employee_id
             , employee_name
             , department_name)
     VALUES ( /* employee.employeeId */0
             , /* employee.employeeName */'name'
             , <error descr="Bind variables must be followed by test data">/* employee.department */</error>department)
