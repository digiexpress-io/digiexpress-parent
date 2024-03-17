select 
  json_agg(result)
from (
	select
	  current.id || ''  as external_id,
	  current.name as group_name,
	  
	  COALESCE(parent.name, parent_2.name) as parent_group_name,
	  COALESCE(parent.id || '', parent_2.ext_id) as parent_group_external_id
	  
	from location as current
	left join location as parent on(current.parent_location_id = parent.id)
	left join (select id, (name || '::tenant') as name, (id || '::tenant') as ext_id from tenant) as parent_2 on(current.tenant_id = parent_2.id)
	
	order by parent_group_name DESC, group_name
) as result
	                                                        
