DELETE FROM x
 WHERE id IN ( SELECT id
                 FROM x2
                WHERE id > /* id */101
                  AND div = 't' )
