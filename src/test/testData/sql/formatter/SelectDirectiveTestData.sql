SELECT /*%expand */ *
, /*%populate */
, extra
  FROM ( SELECT * FROM users ) AS u
               , ( SELECT tag  FROM post WHERE u.usr_id = /*authr.id*/0 ) AS tag          , employee
where common_id = /*@example.status.CommonStatus@id*/1
and 
(post = /*#null */
   or sub_title = /*^subTitle*/'subTitle')