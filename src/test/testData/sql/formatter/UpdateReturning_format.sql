UPDATE user
   SET name = /* user.name */'name'
       , rank = /*user.rank */3
 WHERE id = /* user.id */1
RETURNING id
          , name
          , rank
