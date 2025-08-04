SELECT *  FROM users
WHERE /*%for condition : searchConditions */
  AND 
( /*%for field : condition.fields */
    OR   /* field.name */name LIKE /* field.pattern */'%test%'
  /*%end*/) /*%end*/