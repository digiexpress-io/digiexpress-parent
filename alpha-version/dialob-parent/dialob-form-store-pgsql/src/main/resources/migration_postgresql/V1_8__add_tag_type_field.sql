alter table form_rev 
	add column type char(7) default 'NORMAL'
@
alter table form_rev 
	add column ref_name varchar(128) default null
@

alter table form_rev 
	add constraint form_rev_ref_name_fkey foreign key(tenant_id, form_name, ref_name) references form_rev(tenant_id, form_name, name) on update restrict on delete restrict
@

alter table form_rev_archive 
	add column type char(7) default 'NORMAL'
@
alter table form_rev_archive 
	add column ref_name varchar(128) default null
@

CREATE OR REPLACE FUNCTION archive_form_rev() RETURNS trigger AS $$
BEGIN
   IF (TG_OP = 'DELETE') THEN
	   INSERT INTO form_rev_archive (deleted, tenant_id, form_name, name, created, updated, form_document_id, type, ref_name) values (current_timestamp, OLD.tenant_id, OLD.form_name, OLD.name, OLD.created, OLD.updated, OLD.form_document_id, OLD.type, OLD.ref_name); 
	   RETURN OLD;
   END IF;
   INSERT INTO form_rev_archive (tenant_id, form_name, name, created, updated, form_document_id, type, ref_name) values (OLD.tenant_id, OLD.form_name, OLD.name, OLD.created, OLD.updated, OLD.form_document_id, OLD.type, OLD.ref_name); 
   RETURN NEW;
END;
$$ LANGUAGE plpgsql
@

CREATE OR REPLACE FUNCTION archive_form() RETURNS trigger AS $$
DECLARE
  now_ts timestamp;
BEGIN
   now_ts = current_timestamp;
   INSERT INTO form_archive (deleted, tenant_id, name, created, updated, latest_form_id, label) values (now_ts, OLD.tenant_id, OLD.name, OLD.created, OLD.updated, OLD.latest_form_id, OLD.label);
   DELETE FROM form_rev WHERE form_name = OLD.name; 
   RETURN OLD;
END;
$$ LANGUAGE plpgsql
@

CREATE TRIGGER archive_form_rev_on_update 
	BEFORE UPDATE OR DELETE ON form_rev FOR EACH ROW 
	EXECUTE PROCEDURE archive_form_rev()
@
