SELECT u.name, tag
  FROM users u , lateral ( SELECT *
FROM post WHERE content = 'XXX' ) AS tag , employee