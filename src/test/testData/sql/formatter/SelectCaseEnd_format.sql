SELECT *
       , CASE
              /** case-end */
              WHEN SUM(CASE
                            WHEN name = 'apple' THEN 1
                            WHEN name = 'berry' THEN 3
                            ELSE 0
                        END) > 0 THEN 'x'
              -- active check
              WHEN SUM(CASE
                            WHEN NOT EXISTS ( SELECT number
                                                FROM users
                                               WHERE status = 1 ) THEN 1
                            ELSE 0
                        END) > 0 THEN 'y'
              WHEN SUM(CASE
                            WHEN position > 100
                                 AND count(*) > 10 THEN 1
                                  OR number > 10 THEN 1
                            WHEN position <= 100
                                  OR count(*) > 10 THEN 1
                            ELSE 0
                        END) > 0 THEN 'z'
              ELSE 0
          END AS user_status
  FROM active_users
 ORDER BY CASE
               WHEN orders = 1 THEN 1
               ELSE 2
           END
          , CASE
                 WHEN pickup IS NULL THEN
                      CASE
                           WHEN case_status THEN 0
                           ELSE 1
                       END
                 ELSE
                      CASE
                           WHEN case_static_status THEN 0
                           ELSE 1
                       END
             END
