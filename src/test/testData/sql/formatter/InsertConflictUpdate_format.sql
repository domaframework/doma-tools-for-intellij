INSERT INTO employees
            (name
             , manager_id)
SELECT name
       , user_id
  FROM user_settings
 WHERE user_id = /*employee.id*/0
   AND name = /*employee.name*/'name'
ON CONFLICT (id)
DO UPDATE
      SET name = EXCLUDED.name
          , email = DEFAULT
    WHERE employees.name IS DISTINCT FROM EXCLUDED.name
RETURNING id
          , manager_id
          , name
