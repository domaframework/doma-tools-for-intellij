select * from projects
-- project : Optional<Project> -> Project
where id = /* project.projectId */0
  and project_name = /* project.<error descr="The field or method [flatMap] does not exist in the class [Project]">flatMap</error>() */'projectName'
  and sa_number = /* id.<error descr="The field or method [flatMap] does not exist in the class [Integer]">flatMap</error>() */0
  and number = /* id.MAX_VALUE */0
  and as_long = /* longId.<error descr="The field or method [getAsLong] does not exist in the class [Long]">getAsLong</error>() */0
  and long = /* longId.doubleValue() */0
  and as_double = /* doubleId.<error descr="The field or method [getAsDouble] does not exist in the class [Double]">getAsDouble</error>() */0
  and doubles = /* longId.doubleValue() */0
  -- optId : Optional<Integer> -> Integer
 /*%for optId : project.optionalIds */
  OR  sub_id = /* optId.MAX_VALUE */0
 /*%end */