SELECT p.id
       , p.name
       , p.description
  FROM project p
 WHERE p.id = /* id */1
   /*%if searchName != null */
   AND p.name LIKE /* <error descr="The bind variable [options] does not exist in the DAO method [selectSelectOption]">options</error> */'%test%'
   /*%end*/
