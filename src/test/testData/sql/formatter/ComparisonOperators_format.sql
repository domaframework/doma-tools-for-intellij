-- JSON operators and comparison operators test
SELECT id
       ,
         -- JSON/JSONB operators
         json_data ->> 'name' AS name
       , json_data -> 'address' ->> 'city' AS city
       , jsonb_data @> '{"active": true}' AS is_active
       , jsonb_data <@ '{"role": "admin"}' AS has_admin_role
       , jsonb_data ? 'email' AS has_email
       , jsonb_data ?& array[/* property1 */'name', /* property2 */'age'] AS has_required_fields
       , jsonb_data ?| array['phone', 'mobile'] AS has_contact
       , jsonb_data #> '{contact,phone}' AS phone_path
       , jsonb_data #>> '{contact,phone}' AS phone_text
       ,
         -- Comparison operators
         price <> 0 AS non_zero_price
       , quantity != 0 AS non_zero_quantity
       , start_date <= end_date AS valid_period
       , priority >= 5 AS high_priority
       , -- Array operators
         tags && ARRAY[/*%for property : properties */
                       /* property */'property'
                         /*%if property_has_next*/
                          ,
                         /*%end*/
                       /*%end*/] AS is_urgent
       , ARRAY['tag1', 'tag2'] <@ tags AS has_all_tags
       , tags && ARRAY['urgent', 'critical'] AS is_urgent
       ,
         -- Pattern matching
         name !~ '^test' AS not_test_name
       , description ~* 'important' AS has_important_desc
       , code !~* 'temp' AS not_temp_code
       ,
         -- Range operators
         date_range @> current_date AS in_range
       , int_range && int4range(1
                          , 10) AS overlaps_range
       ,
         -- Geometric operators
         point <-> point '(0,0)' AS distance_from_origin
       , box @> point '(1,1)' AS contains_point
       ,
         -- Additional comparison combinations
         quantity << 100 AS much_less
       , quantity >> 10 AS much_greater
       ,
         -- Complex conditions with multiple operators
         CASE WHEN json_data ->> 'status' <> 'active' THEN 'inactive'
              WHEN json_data ->> 'priority' >= '5' THEN 'high'
              ELSE 'normal'
          END AS status_priority
  FROM test_table
 WHERE
       -- Multiple consecutive operators in conditions
       json_data ->> 'enabled' = 'true'
   AND jsonb_data @> '{"verified": true}'
   AND price <> 0
   AND quantity >= 10
   AND tags && ARRAY['featured', 'promoted']
   AND date_range @> current_date
   AND name !~* 'test|demo|sample'
 ORDER BY json_data ->> 'priority' DESC
          , price <> 0 DESC
          , id
