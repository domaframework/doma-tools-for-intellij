SELECT e.id
       , e.name
       , d.department_name
  FROM employees e
       /*%if join */
       INNER JOIN separators spr
               ON EXISTS ( SELECT id
                             FROM spr )
       /*%end */
 WHERE
       /*%if isNot*/
       NOT
       /*%end*/
       /*%if filterByHighPerformers */
       EXISTS ( SELECT 1
                  FROM performance_reviews pr
                 WHERE pr.employee_id = e.id
                   /*%if reviewPeriod != null */
                   AND pr.review_date BETWEEN /* reviewPeriod.startDate */'2023-01-01' AND /* reviewPeriod.endDate */'2023-12-31'
                   /*%end*/
                   /*%if minScore != null */
                   AND pr.performance_score >= /* minScore */4.0
                   /*%end*/ )
       /*%elseif filterByLowPerformers */
         /*%if isNot*/
         NOT
         /*%end*/
       EXISTS ( SELECT 1
                  FROM performance_reviews pr
                 WHERE pr.employee_id = e.id
                   /*%if reviewPeriod != null */
                   AND pr.review_date BETWEEN /* reviewPeriod.startDate */'2023-01-01' AND /* reviewPeriod.endDate */'2023-12-31'
                   /*%end*/
                   /*%if minScore != null */
                   AND pr.performance_score >= /* minScore */4.0
                   /*%end*/ )
       /*%elseif filterByMediumPerformers */
       NOT EXISTS ( SELECT 1
                      FROM performance_reviews pr
                     WHERE pr.employee_id = e.id
                       /*%if reviewPeriod != null */
                       AND pr.review_date BETWEEN /* reviewPeriod.startDate */'2023-01-01' AND /* reviewPeriod.endDate */'2023-12-31'
                       /*%end*/
                       /*%if minScore != null */
                       AND pr.performance_score >= /* minScore */4.0
                       /*%end*/ )
       /*%else*/
       e.is_active = true
       /*%end*/
