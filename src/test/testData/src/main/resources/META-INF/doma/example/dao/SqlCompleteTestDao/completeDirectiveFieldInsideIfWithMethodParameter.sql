update employee e set
 e.employee_name = /* employee.employeeName */'name'
 , e.rank = /* employee.rank */1
where
 e.employee_id = /* employee.employeeId */1
 /*%if employee.getFirstProject(employee.employeeId).<caret>pro */
  and e.department = /* employee.department */'department'
 /*%elseif employee.department.startWith("100") */
  and e.sub_department = /* employee.department */'department'
  and e.rank >= /* employee.rank */9999
 /*%else */
  and e.sub_department = /* employee.department */'department'
 /*%end*/