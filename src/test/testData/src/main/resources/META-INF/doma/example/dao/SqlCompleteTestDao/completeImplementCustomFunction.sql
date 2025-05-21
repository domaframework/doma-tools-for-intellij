SELECT * FROM project
WHERE project_id = /* project.projectId */0
AND manager = /* project.manager.userId */0
AND lang = /* @use<caret>() */'en'
