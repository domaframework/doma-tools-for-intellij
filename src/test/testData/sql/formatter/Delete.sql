delete from x where id in (select id from x2 where id > /* id */ 101 and div = 't')
