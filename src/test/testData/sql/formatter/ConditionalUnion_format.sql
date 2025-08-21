/*%if includeEmployees */
SELECT 'Employee' AS type
       , e.id
       , e.name
       , e.email
  FROM employees e
 WHERE e.is_active = true
   /*%if departmentId != null */
   AND e.department_id = /* departmentId */1
   /*%end*/
/*%end*/
/*%if includeEmployees && includeContractors */
UNION ALL
/*%end*/
/*%if includeContractors */
SELECT 'Contractor' AS type
       , c.id
       , c.name
       , c.email
  FROM contractors c
 WHERE c.contract_end_date >= CURRENT_DATE
   /*%if projectId != null */
   AND c.project_id = /* projectId */10
   /*%end*/
/*%end*/
/*%if (includeEmployees || includeContractors) && includeVendors */
UNION ALL
/*%end*/
/*%if includeVendors */
SELECT 'Vendor' AS type
       , v.id
       , v.company_name AS name
       , v.contact_email AS email
  FROM vendors v
 WHERE v.status = 'active'
   /*%if vendorType != null */
   AND v.vendor_type IN /* vendorType */('supplier', 'service')
   /*%end*/
/*%end*/
ORDER BY type
         , name
