select
   p.project_id
   , p.statis
   , p.project_name
   , p.rank
from project p
 inner join project_detail pd
  on p.project_id = pd.project_id
 where
  p.project_id = /* detail.projectId */0
  /*%if @isNotEmpty(detail.members) */
  -- Code completion for static fields and methods
  and pd.type = /* @<caret>@ */'TODO'
    /*%for member: detail.members */
      and pd.member_id = /* member.memberId */0
    /*%end */
  /*%end */