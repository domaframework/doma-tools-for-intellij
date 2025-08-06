Select case when div = 'A' then 'AAA'
/*%if addCondition*/
 /*%if conditionType == 1 */when div = 'B' then 'BBB1'
/*%elseif conditionType == 2 */
when div = 'B' then 'BBB2'
/*%end*/
/*%end*/
 else 'CCC' end as divName
from users