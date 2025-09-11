/**
* Method parameter checking: Valid cases
* - Cases called directly as bind variables
* - Cases called within method parameters
* - Cases called in the middle of field access (instance methods and static methods only)
* - Combinations with instance methods, static methods, custom functions, and built-in functions
*/
SELECT *
  FROM employee
 WHERE
       /** Static method parameter count check */ id = /* @doma.example.entity.Project@calculateCost(0, employee.managerId) */0
   AND user_name = /* @doma.example.entity.Project@manager.processText(employee.employeeName).isBlank() */false
   /** Method return values within parameters are also recognized and used */
   AND (name = /* employee.employeeParam(employee.employeeName, project.optionalIds.get(0)) */0
         -- Overload
         OR name = /* employee.employeeParam(project.projectId, floatValue) */0)
   /** Subtypes are allowed */
   AND cost = /* employee.getSubEmployee(subProject).cost */0
    -- Definition: int Parameter type: Integer
    OR (id = /* @doma.example.entity.Project@getEmployee(employee.managerId).managerId */0
         -- Overload: Definition: Integer Parameter type: int
         OR id = /* @doma.example.entity.Project@getEmployee(floatValue, intValue).managerId */0
        -- Definition: CharSequence Parameter: String
        AND description = /* @doma.example.entity.Project@formatName(str, str) */''
)
   AND cost = /* @doma.example.entity.Project@getProjectNumber(employee.managerId, subProject.number) */0
   /** Custom functions */
   AND (empty = /* @isGuest(employee) */false
         -- Overload
         OR empty = /* @isGuest(project) */false)
   -- Subtype
   AND id = /*@isGuestInProject(subProject)*/false
    /** Built-in functions */
    OR (empty = /* @roundUpTimePart(localDate) */'2099-12-31'
         -- Overload
         OR empty = /* @roundUpTimePart(localDateTime) */'2099-12-31')
   -- Valid: Literal characters are recognized as String
   AND name = /* @doma.example.entity.Project@formatName(charSeq.toString(), "suffix") */''
    OR name = /* @doma.example.entity.Project@getEmployee(0).employeeName */''
   -- Valid: Method return value types within parameters are recognized
   AND user_desc = /* @doma.example.entity.Project@formatName(@suffix(employee.employeeName), employee.processText("test")) */''
 /** Parameter checking for elements defined in loop directives */
 ORDER BY
         /*%for item : columns */
          /* item.params().get(0).currentYear() */'2025-12-31'
           /*%if item_has_next*/
            ,
           /*%end*/
         /*%end*/
