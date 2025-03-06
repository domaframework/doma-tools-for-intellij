select
 e.employee_id
 , e.employee_name
 , e.rank
from employee e
 inner join project p on e.employee_id = p.employee_id
where e.employee_id = /* summary.employeeId */1
/*%if @doma.example.entity.Project@rank == summary.employee.<caret>r  */
 and e.project_cost >= /* @doma.example.entity.Project@cost */9999
/*%end */