-- Using Dao arguments other than BiFunction as bind variables
select
 e.employee_id
 , e.employee_name
 , e.employee_number
 from  employee e
  inner join project p
   on p.project_id = e.project_id
 where p.project_id = /* id */1
