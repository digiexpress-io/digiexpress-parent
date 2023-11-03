create table form_document (
  id uuid not null,
  rev integer not null,
  created timestamp not null default current_timestamp,
  updated timestamp not null default current_timestamp,
  data jsonb,
  primary key(id));

create table questionnaire(
  id uuid not null,
  rev integer not null, 
  created timestamp not null default current_timestamp, 
  updated timestamp not null default current_timestamp, 
  data jsonb,
  primary key(id));
