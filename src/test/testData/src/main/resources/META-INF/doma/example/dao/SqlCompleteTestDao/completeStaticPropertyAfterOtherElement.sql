select
   p.project_id
   , p.statis
   , p.project_name
   , p.rank
from project p
 inner join project_detail pd
  on p.project_id = pd.project_id
 where
  /*%for userId : userIds */
   pd.manager_id = /* id @doma.example.entity.ProjectDetail@<caret> */'TODO'
   /*%if userId_has_next */
    /*# "OR" */
   /*%end */
  /*%end */