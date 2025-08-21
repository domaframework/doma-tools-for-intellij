SELECT id, name, department_id
FROM employees
WHERE 
/*%if departmentIds != null */
department_id IN /* departmentIds */(1, 2, 3)
/*%end*/
/*%if statusList != null */
AND status IN /* statusList */('active', 'pending', 'inactive')
/*%end*/
/*%if categoryIds != null && categoryIds.size() > 0 */
AND category_id IN /* categoryIds */(100, 200, 300, 400, 500)
/*%end*/