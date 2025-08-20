SELECT * FROM products p
WHERE 1 = 1
/*%if productName != null */
AND p.name LIKE /* productName */'%laptop%'
/*%end*/
/*%if minPrice != null */
AND p.price >= /* minPrice */1000
/*%end*/
/*%if maxPrice != null */
AND p.price <= /* maxPrice */5000
/*%end*/
/*%if categoryIds != null && categoryIds.size() > 0 */
AND p.category_id IN /* categoryIds */(1, 2, 3)
/*%end*/
/*%if status != null */
/*%if status == "available" */
AND p.stock_quantity > IN /* quantitys */(0, 1, 2) AND p.is_active = true
/*%elseif status == "outofstock" */
AND p.stock_quantity = IN /* quantitys */(0, 1, 2)
/*%else*/
AND p.is_active = false
/*%end*/
/*%end*/
ORDER BY p.created_at DESC