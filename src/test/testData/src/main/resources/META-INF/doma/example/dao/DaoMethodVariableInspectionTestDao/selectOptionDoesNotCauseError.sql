-- Using Dao arguments other than SelectOption as bind variables
select
 p.project_id
 , p.project_name
 , p.project_number
from project p
where p.project_name = /* searchName */'name'
 and num = 1 + 3