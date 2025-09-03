SELECT CASE
            /*%if isCheckStatus */
            WHEN status = '1' THEN 1
            /*%else */
              /*%if hasPoint */
              WHEN status = '2' THEN 2
              /*%end*/
            /*%end*/
            WHEN div = 'A' THEN 'AAA'
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
 ORDER BY CASE
               WHEN pickup IS NULL THEN
                    /*%if isCase*/
                    CASE
                         WHEN case_status = 1 THEN 0
                         ELSE 1
                     END
                    /*%elseif isCaseEnd*/
                    CASE
                         WHEN case_status = 2 THEN 0
                         ELSE 1
                     END
                    /*%else*/
                    0
                    /*%end*/
               ELSE
                    CASE
                         WHEN case_static_status THEN 0
                         ELSE 1
                     END
           END
