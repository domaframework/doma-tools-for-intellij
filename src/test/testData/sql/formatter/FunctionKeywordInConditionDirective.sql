-- PostgreSQL
SELECT extract(YEAR FROM e.hire_date) AS hire_year FROM employees e
SELECT extract(MONTH FROM e.hire_date) AS hire_month FROM employees e
-- Oracle
SELECT TO_DATE('2024-01-01', 'YYYY-MM-DD') AS new_year FROM dual
SELECT TO_CHAR(e.hire_date, 'YYYY-MM') AS hire_month FROM employees e
SELECT CURRENT_DATE FROM dual
--MySQL
SELECT YEAR(e.hire_date) AS hire_year FROM employees e
SELECT MONTH(e.hire_date) AS hire_month FROM employees e
SELECT DATE_FORMAT(e.hire_date, '%Y-%m') AS hire_month FROM employees e
-- SQL Server
    SELECT DATEPART(YEAR, e.hire_date) AS hire_year FROM employees e
SELECT DATEPART(MONTH, e.hire_date) AS hire_month FROM employees e
SELECT CONVERT(VARCHAR, e.hire_date, 23) FROM employees e
-- SQLite
    SELECT strftime('%Y', e.hire_date) AS hire_year FROM employees e
SELECT strftime('%m', e.hire_date) AS hire_month FROM employees e
-- Standard SQL
SELECT CURRENT_TIMESTAMP FROM employees 
SELECT CAST(e.salary AS INTEGER) FROM employees e