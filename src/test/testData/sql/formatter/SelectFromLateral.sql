SELECT u.name
       , tag
  FROM lateral ( SELECT * FROM users ) AS u
               , ( SELECT tag  FROM post WHERE u.usr_id = auther ) AS tag          , employee