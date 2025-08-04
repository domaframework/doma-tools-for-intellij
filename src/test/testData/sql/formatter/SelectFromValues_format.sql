SELECT user_id
  FROM ( SELECT user_id
                , name
           FROM employee
                , ( VALUES
                    (20000000001, 'John')
                    , (20000000002 'Tom')
                    , (20000000003, 'Anna')
                  ) T (user_id, name) ) u
