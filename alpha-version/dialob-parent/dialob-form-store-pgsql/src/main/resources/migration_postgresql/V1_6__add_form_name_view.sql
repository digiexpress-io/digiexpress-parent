CREATE OR REPLACE VIEW form_id_to_name AS
	SELECT tenant_id,   latest_form_id as id,              name, null as label, created, updated, CAST(NULL as timestamp) as deleted from form
	UNION ALL
	SELECT tenant_id, form_document_id as id, form_name as name, name as label, created, updated, CAST(NULL as timestamp) as deleted from form_rev
	UNION ALL
	SELECT tenant_id,   latest_form_id as id,              name, null as label, created, updated,                            deleted from form_archive
	UNION ALL
	SELECT tenant_id, form_document_id as id, form_name as name, name as label, created, updated,                            deleted from form_rev_archive;
