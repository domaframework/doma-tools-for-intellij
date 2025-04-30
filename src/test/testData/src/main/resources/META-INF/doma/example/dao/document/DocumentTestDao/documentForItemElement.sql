SELECT *
FROM employees
WHERE 1=1
/*%for employ<caret>eeIds : employeeIdsList */
 -- employeeIds -> List<List<Integer>>
  /*%for ids : employeeIds */
    -- ids -> List<Integer>
    /*%if ids.size < 100 */
      -- id -> Integer
      /*%for id : ids */
       OR department = /* id */
      /*%end */
    /*%else*/
       OR departments = /* ids */(1,2,3)
    /*%end */
  /*%end */
/*%end */