
select json_agg(result)
from (
	select 
	  location_id || '' as group_external_id,
	  user_id || '' as user_external_id,
	  type || '' as role_external_id
	from role
	order by location_id, user_id
) as result