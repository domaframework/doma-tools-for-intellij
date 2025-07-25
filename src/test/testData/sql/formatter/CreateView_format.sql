CREATE VIEW EmployeeView
AS
  SELECT employeeid
         , firstname
         , lastname
         , hiredate
    FROM employee
   WHERE salary > 50000
