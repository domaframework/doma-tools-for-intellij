Select case /*%if isCheckStatus */
    WHEN status = '1' THEN 1
    /*%else */
      /*%if hasPoint */
      WHEN status = '2' THEN 2
      /*%end*/
    /*%end*/
when div = 'A' then 'AAA'
/*%if addCondition*/
 /*%if conditionType == 1 */when div = 'B' then 'BBB1'
/*%elseif conditionType == 2 */
when div = 'B' then 'BBB2'
/*%end*/
/*%end*/
 else 'CCC' end as divName
from users
 ORDER BY CASE
WHEN pickup IS NULL THEN
/*%if isCase*/
CASE
     WHEN case_status = 1 THEN 0
     ELSE 1
 END
/*%elseif isCaseEnd*/
                    CASE
WHEN case_status = 2 THEN 0 ELSE 1  END
 /*%else*/
0
/*%end*/
ELSE CASE
WHEN case_static_status THEN 0          ELSE 1
END END