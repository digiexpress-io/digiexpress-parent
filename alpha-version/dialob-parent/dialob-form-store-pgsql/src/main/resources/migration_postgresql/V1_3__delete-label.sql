create table form_archive (
  deleted timestamp not null default current_timestamp,
  tenant_id char(36) not null,
  name varchar(32) not null,
  created timestamp not null,
  updated timestamp not null,
  latest_form_id uuid not null references form_document(id) on delete cascade,
  label varchar(255) default '',
  primary key(deleted, tenant_id, name)
)@
  
create table form_rev_archive (
  deleted timestamp not null default current_timestamp,
  tenant_id char(36) not null,
  form_name varchar(32) not null,
  name varchar(255) not null default '',
  created timestamp not null,
  updated timestamp not null,
  form_document_id uuid not null references form_document(id) on delete restrict,
  primary key (deleted, tenant_id, form_name, name)
)@

CREATE OR REPLACE FUNCTION archive_form() RETURNS trigger AS $$
DECLARE
  now_ts timestamp;
BEGIN
   now_ts = current_timestamp;
   INSERT INTO form_archive (deleted, tenant_id, name, created, updated, latest_form_id, label) values (now_ts, OLD.tenant_id, OLD.name, OLD.created, OLD.updated, OLD.latest_form_id, OLD.label);
   INSERT INTO form_rev_archive (deleted, tenant_id, form_name, name, created, updated, form_document_id) 
   	 SELECT now_ts, tenant_id, form_name, name, created, updated, form_document_id FROM form_rev WHERE form_name = OLD.name;
   DELETE FROM form_rev WHERE form_name = OLD.name; 

   RETURN OLD;
END;
$$ LANGUAGE plpgsql@


CREATE TRIGGER archive_form_before_delete 
	BEFORE DELETE ON form FOR EACH ROW 
	EXECUTE PROCEDURE archive_form()

