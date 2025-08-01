SELECT employee_id
       , employee_name
       , salary
       , salary * /* yearBonusRate */1.2 AS yearly_bonus
       , salary + (salary * /* @raiseRate() */0.05) AS new_salary
  FROM employee
 WHERE department_id = /* departmentId */1
   /*%if  @minSalary() + extraAmount*/
   AND salary > /* @minSalary() + extraAmount */50000
   /*%elseif @minSalary() - @example.util.Status@status.value() */
     /*%for current : currentParam.params() */
     AND hire_date >= /* current.currentYear() - yearsBack */2020
     /*%end*/
   /*%end*/
