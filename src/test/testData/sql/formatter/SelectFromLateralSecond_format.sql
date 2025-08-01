SELECT u.name
       , tag
  FROM users u
       , LATERAL ( SELECT *
                     FROM post
                    WHERE content = 'XXX' ) AS tag
       , employee
