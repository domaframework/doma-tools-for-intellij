DELETE FROM user_session s
 WHERE s.count = ( SELECT COUNT(*)
                     FROM user u
                    WHERE u.id = /* id */1
                      AND u.session_id = u.id
                      AND u.time_stamp < /* current */'2099-12-31 00:00:00' )
    OR EXISTS ( SELECT u.id
                  FROM user u
                 WHERE u.id = /* id */1
                   AND u.session_id = u.id
                   AND u.time_stamp < /* current */'2099-12-31 00:00:00' )
