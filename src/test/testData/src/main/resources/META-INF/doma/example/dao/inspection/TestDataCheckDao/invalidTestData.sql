INSERT INTO employee
            (employee_id
             , employee_name
             , department_name)
     VALUES ( /* employee.employeeId */0
             , /* employee.employeeName */'name'
             , <error descr="Test data is required after a bind variable directive or a literal variable directive">/* employee.department */</error>department)
