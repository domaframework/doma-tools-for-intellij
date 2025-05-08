Select COUNT(*) from employee
 where rank = /* @<error descr="A non-existent package or class name was specified. [doma.example.entity.EmployeeBase.Rank]">doma.example.entity.EmployeeBase.Rank</error>@MANAGER */1
   and exist_class = /* @<error descr="A non-existent package or class name was specified. [doma.example.entity.Employee.Exists]">doma.example.entity.Employee.Exists</error>@T */false
   and exist_pkg = /* @<error descr="A non-existent package or class name was specified. [doma.example.entity]">doma.example.entity</error>@T */false