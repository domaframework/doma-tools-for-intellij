SELECT CASE WHEN div = 'A' THEN 'AAA'
            /*%if addCondition*/
              /*%if conditionType == 1 */
              WHEN div = 'B' THEN 'BBB1'
              /*%elseif conditionType == 2 */
              WHEN div = 'B' THEN 'BBB2'
              /*%end*/
            /*%end*/
            ELSE 'CCC'
        END AS divName
  FROM users
