with tables AS ( ( SELECT top, no_pre_as AS AS_NAME, pre_as, clm3 from demo
WHERE id = /*# "block" */ )
UNION ALL ( SELECT id2, no_pre_as2 AS AS_NAME2, pre_as2 FROM demo2
          WHERE id2 = /*# "block2" */ ) )
SELECT query.id3
       , query.no_pre_as3 AS AS_NAME3
       , query.pre_as3
  FROM demo3 query
       INNER JOIN query1 q1 ON q1.id = query.id3
       LEFT JOIN query1 q2 ON query.id3 = q2.id AND query.pre_as3 = q2.sub_id
 WHERE query.id3 = /* id */ 1
 ORDER BY query.id3, query.pre_as3 
