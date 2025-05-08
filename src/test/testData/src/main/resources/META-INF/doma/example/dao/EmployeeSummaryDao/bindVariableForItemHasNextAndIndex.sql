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
       and p.not_next = /* member_has_next.<error descr="The field or method [NotTRUE] does not exist in the class [Boolean]">NotTRUE</error> */false
       and p.next = /* member_has_next.TRUE */false
       and p.not_index = /* member_index.<error descr="The field or method [nextValue] does not exist in the class [Integer]">nextValue</error>() */999
       and p.index = /* member_index.MIN_VALUE */0
     /*%end */
        p.employee_id = /* employees.get(0).employeeId */0
   /*%end */