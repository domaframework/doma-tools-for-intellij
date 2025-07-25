SELECT user_id
  FROM ( SELECT user_id
                , name
           FROM employee emp
                , ( VALUES
                    /*# usersList */
                  ) T (user_id, name) ) u
