ALTER TABLE process ALTER created type timestamp with time zone using created at time zone 'UTC';
ALTER TABLE process ALTER updated type timestamp with time zone using updated at time zone 'UTC';
ALTER TABLE process ALTER expires_at type timestamp with time zone using expires_at at time zone 'UTC';