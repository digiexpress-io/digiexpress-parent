
select json_agg(all_users) from(
	select 
	  id || '' as external_id,
	  lower(coalesce(first_name, 'anonymous')) as first_name,
	  
	  lower(coalesce(substring(last_name, 1, 1), 'a')) || 
	  substring(user_id, 1, 1) as last_name
	from 
	  users
) all_users;
                                  
