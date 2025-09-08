SELECT /*%expand */*
  FROM ( SELECT *
           FROM users ) AS u
       , ( SELECT tag
             FROM post
            WHERE u.usr_id = /* authr.id */0 ) AS tag
       , employee
 WHERE common_id = /* @example.status.CommonStatus@id */1
   AND
       /*%if insertCondition */
       post_title = /* title */'title'
       /*%else */
       (post = /*# null */
         OR sub_title = /*^ subTitle */'subTitle')
       /*%end*/
