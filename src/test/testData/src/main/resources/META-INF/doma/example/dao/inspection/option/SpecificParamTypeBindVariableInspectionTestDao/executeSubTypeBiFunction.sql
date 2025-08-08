SELECT COUNT(*) AS record_count
       , MAX(id) AS max_id
       , MIN(id) AS min_id
  FROM /* tableName */'employee'
 WHERE active = /* <error descr="The bind variable [func] does not exist in the DAO method [executeSubTypeBiFunction]">func</error> */true