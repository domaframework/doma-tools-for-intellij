SELECT e.id
, e.name
,
/*%if includeSkillCount */
( SELECT COUNT(DISTINCT es.skill_id)
FROM employee_skills es
/*%if onlyActiveSkills */
INNER JOIN skills sk ON es.skill_id = sk.id
WHERE es.employee_id = e.id
AND sk.is_active = true
/*%if skillCategories != null && skillCategories.size() > 0 */
AND sk.category IN /* skillCategories */('technical', 'management')
/*%end*/
/*%else*/
WHERE es.employee_id = e.id
/*%end*/ ),
/*%end*/
d.department_name
FROM employees e
INNER JOIN departments d ON e.department_id = d.id
 WHERE/*%if filterByHighPerformers */
ids IN ( SELECT id
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
/*%if excludeDepartments != null && excludeDepartments.size() > 0 */
AND e.department_id NOT IN ( SELECT d2.id
FROM departments d2
WHERE d2.name IN /* excludeDepartments */('HR', 'Finance')
/*%if onlyActiveDepartments */
AND d2.is_active = true
/*%end*/ )
/*%end*/
