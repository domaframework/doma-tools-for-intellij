CREATE OR REPLACE VIEW employee_view
AS
  SELECT id
         , name
         , department
         , salary
    FROM employees
   WHERE active = true
