SELECT *
  FROM orders
 WHERE
       /*%if status == "pending" */
       status = 'pending'
       AND created_at >= CURRENT_DATE - INTERVAL '7 days'
       /*%elseif status == "processing" */
       status = 'processing'
       AND assigned_to IS NOT NULL
       /*%elseif status == "completed" */
       status = 'completed'
       AND completed_at IS NOT NULL
       /*%else*/
       status IN ('cancelled', 'refunded')
       /*%end*/
