select * from employee e
 inner join project p
  on p.employee_id = e.employee_id
 where p.employee_id IN /* proj<caret>ect.members */(0,1,2)