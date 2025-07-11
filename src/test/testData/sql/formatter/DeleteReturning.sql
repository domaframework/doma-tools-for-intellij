DELETE FROM x
 WHERE id IN ( SELECT id, name
                 FROM x2
                WHERE id > /* id */101 AND div = 't' ) 
returning id, name
