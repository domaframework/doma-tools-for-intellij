SELECT CASE WHEN div = 'A' THEN 'AAA'
            WHEN div = 'B' THEN 'BBB'
            ELSE 'CCC'
        END AS divName
  FROM users
