update employee e set
 e.employee_name = /* employee.employeeName */'name'
 , e.rank = /* employee.rank */1
where
 e.employee_id = /* employee.employeeId */1
 /*%if employee.department.startWith("200")  */
  and e.department = /* employee.department */'department'
 /*%el<caret> employee.department.startWith("100") */
  and e.sub_department = /* employee.department */'department'
  and e.rank >= /* employee.rank */9999
 /*%else */
  and e.sub_department = /* employee.department */'department'
 /*%end*/