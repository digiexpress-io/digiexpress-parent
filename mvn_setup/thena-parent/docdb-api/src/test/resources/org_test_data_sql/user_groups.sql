
select json_agg(result)
from (
	select 
	  location_id || '' as group_external_id,
	  user_id || '' as user_external_id
	from user_locations
	order by location_id, user_id
) as result