select
   p.project_id
   , p.statis
   , p.project_name
   , p.rank
from project p
 inner join project_detail pd
  on p.project_id = pd.project_id
 where
  -- Code completion for static fields and methods
  and pd.manager_id = /* @doma.example.entity.ProjectDetail@getTerm<caret>Number() */'TODO'