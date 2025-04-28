-- Use List-type Dao argument directly as a List-type bind variable
  select p.project_id
   , pd.project_detail_id
   , pd.project_number
   , p.project_name
   , e.employee_id
   , e.employee_name
   from project_detail pd
   inner join project p
    on p.project_id = pd.project_id
   inner join employee e
    on pd.employee_id = e.employee_id
    -- Use as List-type
   /*%if employees.size() > 0 */
     where
     /*%for member : employees */
        p.employee_id = /* member.<error descr="The field or method [employee_id] does not exist in the class [Employee]">employee_id</error> */0
     /*%end */
   -- Cannot be used as a class inside List-type
   /*%elseif employees.<error descr="The field or method [rank] does not exist in the class [List]">rank</error> > 3*/
      p.employee_id = /* employees.<error descr="The field or method [employeeId] does not exist in the class [List]">employeeId</error> */0
      and p.base_rank = /* employees.<error descr="The field or method [rank] does not exist in the class [List]">rank</error> */0
   /*%end */