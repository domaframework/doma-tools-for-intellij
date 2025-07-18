SELECT e.id
       , e.name
       , d.name AS department_name
  FROM employee e
       /*%if includesDepartment */
       LEFT JOIN department d
              ON e.department_id = d.id
         /*%if includeDepartmentDetails */
         LEFT JOIN department_detail dd
                ON d.id = dd.department_id
         /*%end */
       /*%end */
 WHERE 1 = 1
 ORDER BY /*%if sortConditions != null && !sortConditions.isEmpty() */ -- IF1
   /*%for sort : sortConditions */ -- IF2
     /*%if sort.field == "name" */ -- IF3
     e.name
       /*%if sort.direction == "DESC" */ -- IF4
       DESC
       /*%else */ -- ELSE4
       ASC
       /*%end */ -- END4
     /*%elseif sort.field == "department" */ -- ELSE3
       /*%if includesDepartment */ -- IF5
       d.name
         /*%if sort.direction == "DESC" */ -- IF6
         DESC
         /*%else */ -- ELSE6
         ASC
         /*%end */ -- END6
       /*%else */ -- ELSE5
       e.department_id
         /*%if sort.direction == "DESC" */ -- IF7
         DESC
         /*%else */ -- ELSE7
         ASC
         /*%end */ -- END7
       /*%end */ -- END5
     /*%end */ -- END3
     /*%if sort_has_next */ -- IF8
      ,
     /*%end */ -- END8
   /*%end */ -- END2
 /*%else */ -- ELSE1
 e.id ASC
 /*%end */ -- END1 
