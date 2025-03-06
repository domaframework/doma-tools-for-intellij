update employee e set
 e.employee_name = /* employee.employeeName */'name'
 , e.rank = /* employee.rank */1
where
 e.employee_id = /* employee.employeeId */1
 /*%for member : project.<caret> */
   /*%if member.department.startWith("200")  */
    and e.department = /* member.department */'department'
   /*%elseif member.department.startWith("100") */
    and e.sub_department = /* member.department */'department'
    and e.rank >= /* member.rank */9999
   /*%else */
    and e.sub_department = /* member.department */'department'
   /*%end*/
  /*%end*/