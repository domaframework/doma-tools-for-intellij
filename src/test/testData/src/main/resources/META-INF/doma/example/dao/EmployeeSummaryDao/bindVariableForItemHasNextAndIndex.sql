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
       /*%if member_has_next */
          /*# "or" */
       /*%end */
           p.employee_id = /* member.employeeId */0
       and p.not_next = /* member_has_next */false
       and p.next = /* member_has_next */false
       and p.not_index = /* member_index */999
       and p.index = /* member_index */0
     /*%end */
        p.employee_id = /* employees.get(0).employeeId */0
   /*%end */