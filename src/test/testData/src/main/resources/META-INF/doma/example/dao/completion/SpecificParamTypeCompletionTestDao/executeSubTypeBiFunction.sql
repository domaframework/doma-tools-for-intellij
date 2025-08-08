SELECT COUNT(*) AS record_count
       , MAX(id) AS max_id
       , MIN(id) AS min_id
  FROM /* <caret> */'employee'
 WHERE active = true
