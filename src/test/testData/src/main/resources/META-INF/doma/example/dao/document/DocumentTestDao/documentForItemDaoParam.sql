SELECT *
FROM employees
WHERE 1=1
-- employeeIdsList -> HashSet<List<List<Integer>>>
/*%for employeeIds : employee<caret>IdsList */
 -- employeeIds -> List<List<Integer>>
  /*%for ids : employeeIds */
    -- ids -> List<Integer>
    /*%if ids.size < 100 */
      -- id -> Integer
      /*%for id : ids */
       AND department = /* id */
      /*%end */
    /*%else*/
       AND departments = /* ids */(1,2,3)
    /*%end */
  /*%end */
/*%end */