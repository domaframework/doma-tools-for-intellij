INSERT INTO employee
            (id
             , username)
     VALUES ( /* employees.id */0
             , /* employees.name */'name')
ON CONFLICT (username) ON CONSTRAINT
DO NOTHING 
