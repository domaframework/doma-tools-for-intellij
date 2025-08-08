SELECT p.id
       , p.name
       , p.description
  FROM project p
 WHERE p.id = /* <caret> */1
   /*%if searchName != null */
   AND p.name LIKE /* searchName */'%test%'
   /*%end*/
