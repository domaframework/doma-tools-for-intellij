/**
* Method parameter count checking: Error cases
* Error: Function [functionName] definition with [X] parameters not found
*/
SELECT *
  FROM employee
 WHERE
       -- Static method parameter count check
       id = /* @doma.example.entity.Project@<error descr="Function getEmployee definition with 3 parameters not found">getEmployee</error>(employee.employeeName, 0,employee.managerId) */0
    -- Instance method parameter count check
    OR id = /* employee.<error descr="Function employeeParam definition with 3 parameters not found">employeeParam</error>(employee.employeeName, project.projectId, 3) */0
   -- Custom function parameter count check
   AND cost = /* @<error descr="Function userName definition with 1 parameters not found">userName</error>(project.cost) */false
    -- Built-in function parameter check
    OR cost = /* @<error descr="Function isNotEmpty definition with 2 parameters not found">isNotEmpty</error>(employee.employeeName, project.projectId) */false
   /** Parameter count check for methods called in the middle */
   -- Static method
   AND id = /* @doma.example.entity.Project@manager.<error descr="Function getUserNameFormat definition with 1 parameters not found">getUserNameFormat</error>(employee).indent("indent") */0
    -- Instance method
    OR id = /* employee.projects.get(0).<error descr="Function getFirstEmployee definition with 3 parameters not found">getFirstEmployee</error>(employee.employeeName, project.projectId, 3).cost */0
   /** Parameter count check for methods called within parameters */
   -- Static method * インスタンスメソッド
   AND static = /* @doma.example.entity.Project@<error descr="Function getEmployee definition with 3 parameters not found">getEmployee</error>(employee.<error descr="Function employeeParam definition with 3 parameters not found">employeeParam</error>(employee.managerId, project.cost, "instance"),project.projectCategory,  "value") */0
    OR static = /* @doma.example.entity.Project@<error descr="Function getEmployee definition with 3 parameters not found">getEmployee</error>(@doma.example.entity.DummyProject@<error descr="Function getTermNumber definition with 1 parameters not found">getTermNumber</error>(0), project.projectCategory, "static") */0
    OR static = /* @doma.example.entity.Project@<error descr="Function getEmployee definition with 3 parameters not found">getEmployee</error>(@<error descr="Function isGuest definition with 2 parameters not found">isGuest</error>(0, employee.employeeName), project.projectCategory,  "custom") */0
    OR static = /* @doma.example.entity.Project@<error descr="Function getEmployee definition with 3 parameters not found">getEmployee</error>(@<error descr="Function isNotBlank definition with 2 parameters not found">isNotBlank</error>(0, employee.employeeName), project.projectCategory,  "built") */0
   -- Instance method
   AND fields = /* employee.<error descr="Function employeeParam definition with 3 parameters not found">employeeParam</error>(project.cost, employee.<error descr="Function processText definition with 2 parameters not found">processText</error>(0, project.projectCategory), "instance") */0
    OR fields = /* employee.<error descr="Function employeeParam definition with 4 parameters not found">employeeParam</error>(project.cost, @doma.example.entity.DummyProject@<error descr="Function getTermNumber definition with 1 parameters not found">getTermNumber</error>(0), "static", "value") */0
    OR fields = /* employee.<error descr="Function employeeParam definition with 4 parameters not found">employeeParam</error>(project.cost, @<error descr="Function isGuest definition with 2 parameters not found">isGuest</error>(0, employee.employeeName), "custom", "value") */0
    OR fields = /* employee.<error descr="Function employeeParam definition with 4 parameters not found">employeeParam</error>(project.cost, @<error descr="Function isNotBlank definition with 2 parameters not found">isNotBlank</error>(0, employee.employeeName), "built", "value") */0
   -- Custom function * Instance method
   AND custom = /* @<error descr="Function userName definition with 1 parameters not found">userName</error>(project.<error descr="Function calculateCost definition with 3 parameters not found">calculateCost</error>(0, employee.managerId, "instance")) */false
    OR custom = /* @<error descr="Function userName definition with 1 parameters not found">userName</error>(@doma.example.entity.DummyProject@<error descr="Function getTermNumber definition with 1 parameters not found">getTermNumber</error>(0)) */false
    OR custom = /* @<error descr="Function userName definition with 2 parameters not found">userName</error>(@<error descr="Function isGuest definition with 2 parameters not found">isGuest</error>(0, employee.employeeName), "custom") */false
    OR custom = /* @<error descr="Function userName definition with 2 parameters not found">userName</error>(@<error descr="Function isNotBlank definition with 2 parameters not found">isNotBlank</error>(0, employee.employeeName), "built") */false
   -- Built-in function * Instance method
   AND built = /* @<error descr="Function isNotEmpty definition with 2 parameters not found">isNotEmpty</error>(@doma.example.entity.DummyProject@<error descr="Function getTermNumber definition with 1 parameters not found">getTermNumber</error>(0), "static") */false
    OR built = /* @<error descr="Function isNotEmpty definition with 2 parameters not found">isNotEmpty</error>(employee.<error descr="Function getUserNameFormat definition with 1 parameters not found">getUserNameFormat</error>(0), "instance") */false
    OR built = /* @<error descr="Function isNotEmpty definition with 2 parameters not found">isNotEmpty</error>(@<error descr="Function isGuest definition with 2 parameters not found">isGuest</error>(0, employee.employeeName), "custom") */false
    OR built = /* @<error descr="Function isNotEmpty definition with 2 parameters not found">isNotEmpty</error>(@<error descr="Function isNotBlank definition with 2 parameters not found">isNotBlank</error>(0, employee.employeeName), "built") */false
 /** Parameter count check for elements defined in loop directives */
 ORDER BY
         /*%for item : columns */
          /* item.<error descr="Function params definition with 3 parameters not found">params</error>(project.cost, employee.<error descr="Function processText definition with 2 parameters not found">processText</error>(0, project.projectCategory), "instance") */0
          , /* item.<error descr="Function params definition with 3 parameters not found">params</error>(project.cost, @doma.example.entity.DummyProject@<error descr="Function getTermNumber definition with 1 parameters not found">getTermNumber</error>(0), "static") */0
          , /* item.<error descr="Function params definition with 3 parameters not found">params</error>(project.cost, @<error descr="Function isGuest definition with 2 parameters not found">isGuest</error>(0, employee.employeeName), "custom") */0
          , /* item.<error descr="Function params definition with 3 parameters not found">params</error>(project.cost, @<error descr="Function isNotBlank definition with 2 parameters not found">isNotBlank</error>(0, employee.employeeName), "built") */0
           /*%if item_has_next*/
            ,
           /*%end*/
         /*%end*/
