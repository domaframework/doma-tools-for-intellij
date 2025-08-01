SELECT p.id, p.name, p.price,
  /*%if includeTax */         p.price * /* taxRate */1.1 AS price_with_tax,  /*%end*/
p.stock_quantity
FROM products p WHERE 1 = 1
  AND p.active = /* isActive */true
  /*%if searchKeyword != null && !searchKeyword.isEmpty() */
    AND (p.name LIKE /* @prefix(searchKeyword) */'test%'
      OR p.description LIKE /* @infix(searchKeyword) */'%test%'
      OR p.sku = /* searchKeyword */'TEST123'
) /*%end*/
  /*%if categories != null && !categories.isEmpty() */
    AND p.category_id IN /* categories */(1, 2, 3)
  /*%end*/
ORDER BY /*%if sortField != null */
    /*%if sortField == "name" */ p.name
    /*%elseif sortField == "price" */ p.price
    /*%else*/ p.created_at
    /*%end*/
/* sortDirection */'ASC'
  /*%else*/
    p.id ASC
  /*%end*/