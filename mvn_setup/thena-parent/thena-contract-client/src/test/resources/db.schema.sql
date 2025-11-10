CREATE TABLE IF NOT EXISTS contract (
  id UUID PRIMARY KEY,
  parent_contract_id UUID,
  contract_number VARCHAR(255) NOT NULL,
  external_id VARCHAR(255),
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  updated_tree_commit_id UUID NOT NULL,
  contract_issue_date DATE NOT NULL,
  contract_start_date DATE NOT NULL,
  contract_maturity_date DATE,
  contract_status VARCHAR(100) NOT NULL,
  contract_sub_status VARCHAR(100),
  contract_type VARCHAR(100) NOT NULL,
  contract_sub_type VARCHAR(100),
  contract_data JSONB
);
CREATE INDEX IF NOT EXISTS contract_STATUS_INDEX ON contract (contract_status);
CREATE INDEX IF NOT EXISTS contract_TYPE_INDEX ON contract (contract_type);
CREATE INDEX IF NOT EXISTS contract_SUB_STATUS_INDEX ON contract (contract_sub_status);
CREATE INDEX IF NOT EXISTS contract_SUB_TYPE_INDEX ON contract (contract_sub_type);
CREATE INDEX IF NOT EXISTS contract_PARENT_INDEX ON contract (parent_contract_id);
CREATE INDEX IF NOT EXISTS contract_COMMIT_INDEX ON contract (commit_id);
CREATE INDEX IF NOT EXISTS contract_CREATED_COMMIT_INDEX ON contract (created_commit_id);
CREATE INDEX IF NOT EXISTS contract_UPDATED_TREE_COMMIT_INDEX ON contract (updated_tree_commit_id);
CREATE TABLE IF NOT EXISTS party (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  external_id VARCHAR(255) NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  party_type VARCHAR(100) NOT NULL,
  party_effective_from DATE NOT NULL,
  party_effective_to DATE,
  party_term_start_date DATE NOT NULL,
  party_term_end_date DATE,
  party_data JSONB
);
CREATE INDEX IF NOT EXISTS party_TYPE_INDEX ON party (party_type);
CREATE INDEX IF NOT EXISTS party_CONTRACT_INDEX ON party (contract_id);
CREATE INDEX IF NOT EXISTS party_COMMIT_INDEX ON party (commit_id);
CREATE INDEX IF NOT EXISTS party_CREATED_COMMIT_INDEX ON party (created_commit_id);
CREATE TABLE IF NOT EXISTS coverage (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  insured_id UUID NOT NULL,
  external_id VARCHAR(255) NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  coverage_type VARCHAR(100) NOT NULL,
  coverage_code VARCHAR(100) NOT NULL,
  coverage_sum_insured DECIMAL(15, 2),
  coverage_rate DECIMAL(10, 6),
  coverage_rate_type VARCHAR(100),
  coverage_status VARCHAR(100) NOT NULL,
  coverage_effective_from DATE NOT NULL,
  coverage_effective_to DATE,
  coverage_term_start_date DATE NOT NULL,
  coverage_term_end_date DATE
);
CREATE INDEX IF NOT EXISTS coverage_TYPE_INDEX ON coverage (coverage_type);
CREATE INDEX IF NOT EXISTS coverage_STATUS_INDEX ON coverage (coverage_status);
CREATE INDEX IF NOT EXISTS coverage_CONTRACT_INDEX ON coverage (contract_id);
CREATE INDEX IF NOT EXISTS coverage_INSURED_INDEX ON coverage (insured_id);
CREATE INDEX IF NOT EXISTS coverage_COMMIT_INDEX ON coverage (commit_id);
CREATE INDEX IF NOT EXISTS coverage_CREATED_COMMIT_INDEX ON coverage (created_commit_id);
CREATE TABLE IF NOT EXISTS capability (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  external_id VARCHAR(255),
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  capability_code VARCHAR(100) NOT NULL,
  capability_name VARCHAR(255) NOT NULL,
  capability_type VARCHAR(100) NOT NULL,
  capability_enabled BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS capability_TYPE_INDEX ON capability (capability_type);
CREATE INDEX IF NOT EXISTS capability_CONTRACT_INDEX ON capability (contract_id);
CREATE INDEX IF NOT EXISTS capability_COMMIT_INDEX ON capability (commit_id);
CREATE INDEX IF NOT EXISTS capability_CREATED_COMMIT_INDEX ON capability (created_commit_id);
CREATE TABLE IF NOT EXISTS reference (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  inv_plan_id UUID,
  inv_plan_alloc_id UUID,
  coverage_id UUID,
  party_id UUID,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  reference_value VARCHAR(255) NOT NULL,
  reference_type VARCHAR(100) NOT NULL,
  reference_body JSONB
);
CREATE INDEX IF NOT EXISTS reference_TYPE_INDEX ON reference (reference_type);
CREATE INDEX IF NOT EXISTS reference_CONTRACT_INDEX ON reference (contract_id);
CREATE INDEX IF NOT EXISTS reference_INV_PLAN_INDEX ON reference (inv_plan_id);
CREATE INDEX IF NOT EXISTS reference_INV_PLAN_ALLOC_INDEX ON reference (inv_plan_alloc_id);
CREATE INDEX IF NOT EXISTS reference_COVERAGE_INDEX ON reference (coverage_id);
CREATE INDEX IF NOT EXISTS reference_PARTY_INDEX ON reference (party_id);
CREATE INDEX IF NOT EXISTS reference_COMMIT_INDEX ON reference (commit_id);
CREATE INDEX IF NOT EXISTS reference_CREATED_COMMIT_INDEX ON reference (created_commit_id);
CREATE TABLE IF NOT EXISTS note (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  inv_plan_id UUID,
  inv_plan_alloc_id UUID,
  coverage_id UUID,
  party_id UUID,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  note_value TEXT NOT NULL,
  note_type VARCHAR(100) NOT NULL,
  note_body JSONB
);
CREATE INDEX IF NOT EXISTS note_TYPE_INDEX ON note (note_type);
CREATE INDEX IF NOT EXISTS note_CONTRACT_INDEX ON note (contract_id);
CREATE INDEX IF NOT EXISTS note_INV_PLAN_INDEX ON note (inv_plan_id);
CREATE INDEX IF NOT EXISTS note_INV_PLAN_ALLOC_INDEX ON note (inv_plan_alloc_id);
CREATE INDEX IF NOT EXISTS note_COVERAGE_INDEX ON note (coverage_id);
CREATE INDEX IF NOT EXISTS note_PARTY_INDEX ON note (party_id);
CREATE INDEX IF NOT EXISTS note_COMMIT_INDEX ON note (commit_id);
CREATE INDEX IF NOT EXISTS note_CREATED_COMMIT_INDEX ON note (created_commit_id);
CREATE TABLE IF NOT EXISTS payment_plan (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  party_id UUID,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  payment_plan_status VARCHAR(100) NOT NULL,
  payment_plan_frequency VARCHAR(100) NOT NULL,
  payment_plan_amount DECIMAL(15, 2) NOT NULL,
  payment_plan_start_date DATE NOT NULL,
  payment_plan_end_date DATE
);
CREATE INDEX IF NOT EXISTS payment_plan_STATUS_INDEX ON payment_plan (payment_plan_status);
CREATE INDEX IF NOT EXISTS payment_plan_FREQUENCY_INDEX ON payment_plan (payment_plan_frequency);
CREATE INDEX IF NOT EXISTS payment_plan_CONTRACT_INDEX ON payment_plan (contract_id);
CREATE INDEX IF NOT EXISTS payment_plan_PARTY_INDEX ON payment_plan (party_id);
CREATE INDEX IF NOT EXISTS payment_plan_COMMIT_INDEX ON payment_plan (commit_id);
CREATE INDEX IF NOT EXISTS payment_plan_CREATED_COMMIT_INDEX ON payment_plan (created_commit_id);
CREATE TABLE IF NOT EXISTS inv_plan (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  external_id VARCHAR(255) NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  inv_plan_status VARCHAR(100) NOT NULL,
  inv_plan_code VARCHAR(100) NOT NULL,
  inv_plan_name VARCHAR(255) NOT NULL,
  inv_plan_start_date DATE NOT NULL,
  inv_plan_end_date DATE
);
CREATE INDEX IF NOT EXISTS inv_plan_STATUS_INDEX ON inv_plan (inv_plan_status);
CREATE INDEX IF NOT EXISTS inv_plan_CODE_INDEX ON inv_plan (inv_plan_code);
CREATE INDEX IF NOT EXISTS inv_plan_CONTRACT_INDEX ON inv_plan (contract_id);
CREATE INDEX IF NOT EXISTS inv_plan_COMMIT_INDEX ON inv_plan (commit_id);
CREATE INDEX IF NOT EXISTS inv_plan_CREATED_COMMIT_INDEX ON inv_plan (created_commit_id);
CREATE TABLE IF NOT EXISTS inv_plan_alloc (
  id UUID PRIMARY KEY,
  inv_plan_id UUID NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  inv_plan_alloc_code VARCHAR(100) NOT NULL,
  inv_plan_alloc_name VARCHAR(255) NOT NULL,
  inv_plan_alloc_percentage DECIMAL(10, 6) NOT NULL,
  inv_plan_alloc_status VARCHAR(100) NOT NULL
);
CREATE INDEX IF NOT EXISTS inv_plan_alloc_STATUS_INDEX ON inv_plan_alloc (inv_plan_alloc_status);
CREATE INDEX IF NOT EXISTS inv_plan_alloc_CODE_INDEX ON inv_plan_alloc (inv_plan_alloc_code);
CREATE INDEX IF NOT EXISTS inv_plan_alloc_INV_PLAN_INDEX ON inv_plan_alloc (inv_plan_id);
CREATE INDEX IF NOT EXISTS inv_plan_alloc_COMMIT_INDEX ON inv_plan_alloc (commit_id);
CREATE INDEX IF NOT EXISTS inv_plan_alloc_CREATED_COMMIT_INDEX ON inv_plan_alloc (created_commit_id);
CREATE TABLE IF NOT EXISTS contract_date_relativity (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  inv_plan_id UUID,
  coverage_id UUID,
  party_id UUID,
  payment_plan_id UUID,
  entity_type VARCHAR(50) NOT NULL,
  field_name VARCHAR(100) NOT NULL,
  relative_to_type VARCHAR(50) NOT NULL,
  offset_interval INTERVAL,
  calculation_rule VARCHAR(100),
  description TEXT
);
CREATE INDEX IF NOT EXISTS contract_date_relativity_CONTRACT_INDEX ON contract_date_relativity (contract_id);
CREATE INDEX IF NOT EXISTS contract_date_relativity_ENTITY_TYPE_INDEX ON contract_date_relativity (entity_type);
CREATE INDEX IF NOT EXISTS contract_date_relativity_INV_PLAN_INDEX ON contract_date_relativity (inv_plan_id);
CREATE INDEX IF NOT EXISTS contract_date_relativity_COVERAGE_INDEX ON contract_date_relativity (coverage_id);
CREATE INDEX IF NOT EXISTS contract_date_relativity_PARTY_INDEX ON contract_date_relativity (party_id);
CREATE INDEX IF NOT EXISTS contract_date_relativity_PAYMENT_PLAN_INDEX ON contract_date_relativity (payment_plan_id);
CREATE TABLE IF NOT EXISTS commit (
  commit_id UUID PRIMARY KEY,
  parent_id UUID,
  contract_id UUID,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  commit_log TEXT NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS commit_PARENT_INDEX ON commit (parent_id);
CREATE INDEX IF NOT EXISTS commit_CONTRACT_INDEX ON commit (contract_id);
CREATE INDEX IF NOT EXISTS commit_AUTH_INDEX ON commit (commit_author);
CREATE TABLE IF NOT EXISTS commit_tree (
  id UUID PRIMARY KEY,
  commit_id UUID NOT NULL,
  operation_type VARCHAR(40),
  body_after JSONB,
  body_before JSONB
);
CREATE INDEX IF NOT EXISTS commit_tree_COMMIT_INDEX ON commit_tree (commit_id);
CREATE SEQUENCE contract_seq MINVALUE 1 MAXVALUE 999999 CYCLE;
ALTER TABLE
  contract
ADD
  CONSTRAINT fk_contract_parent FOREIGN KEY (parent_contract_id) REFERENCES contract(id);
ALTER TABLE
  party
ADD
  CONSTRAINT fk_party_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  coverage
ADD
  CONSTRAINT fk_coverage_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  coverage
ADD
  CONSTRAINT fk_coverage_insured FOREIGN KEY (insured_id) REFERENCES party(id);
ALTER TABLE
  capability
ADD
  CONSTRAINT fk_capability_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  reference
ADD
  CONSTRAINT fk_reference_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  reference
ADD
  CONSTRAINT fk_reference_inv_plan FOREIGN KEY (inv_plan_id) REFERENCES inv_plan(id);
ALTER TABLE
  reference
ADD
  CONSTRAINT fk_reference_inv_plan_alloc FOREIGN KEY (inv_plan_alloc_id) REFERENCES inv_plan_alloc(id);
ALTER TABLE
  reference
ADD
  CONSTRAINT fk_reference_coverage FOREIGN KEY (coverage_id) REFERENCES coverage(id);
ALTER TABLE
  reference
ADD
  CONSTRAINT fk_reference_party FOREIGN KEY (party_id) REFERENCES party(id);
ALTER TABLE
  note
ADD
  CONSTRAINT fk_note_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  note
ADD
  CONSTRAINT fk_note_inv_plan FOREIGN KEY (inv_plan_id) REFERENCES inv_plan(id);
ALTER TABLE
  note
ADD
  CONSTRAINT fk_note_inv_plan_alloc FOREIGN KEY (inv_plan_alloc_id) REFERENCES inv_plan_alloc(id);
ALTER TABLE
  note
ADD
  CONSTRAINT fk_note_coverage FOREIGN KEY (coverage_id) REFERENCES coverage(id);
ALTER TABLE
  note
ADD
  CONSTRAINT fk_note_party FOREIGN KEY (party_id) REFERENCES party(id);
ALTER TABLE
  payment_plan
ADD
  CONSTRAINT fk_payment_plan_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  payment_plan
ADD
  CONSTRAINT fk_payment_plan_party FOREIGN KEY (party_id) REFERENCES party(id);
ALTER TABLE
  inv_plan
ADD
  CONSTRAINT fk_inv_plan_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  inv_plan_alloc
ADD
  CONSTRAINT fk_inv_plan_alloc_inv_plan FOREIGN KEY (inv_plan_id) REFERENCES inv_plan(id);
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT fk_date_relativity_contract FOREIGN KEY (contract_id) REFERENCES contract(id);
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT fk_date_relativity_inv_plan FOREIGN KEY (inv_plan_id) REFERENCES inv_plan(id);
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT fk_date_relativity_coverage FOREIGN KEY (coverage_id) REFERENCES coverage(id);
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT fk_date_relativity_party FOREIGN KEY (party_id) REFERENCES party(id);
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT fk_date_relativity_payment_plan FOREIGN KEY (payment_plan_id) REFERENCES payment_plan(id);
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT check_single_entity CHECK (
    (inv_plan_id IS NOT NULL) :: int + (coverage_id IS NOT NULL) :: int + (party_id IS NOT NULL) :: int + (payment_plan_id IS NOT NULL) :: int = 1
  );
ALTER TABLE
  contract_date_relativity
ADD
  CONSTRAINT check_entity_type_consistency CHECK (
    (
      entity_type = 'INV_PLAN'
      AND inv_plan_id IS NOT NULL
    )
    OR (
      entity_type = 'COVERAGE'
      AND coverage_id IS NOT NULL
    )
    OR (
      entity_type = 'PARTY'
      AND party_id IS NOT NULL
    )
    OR (
      entity_type = 'PAYMENT_PLAN'
      AND payment_plan_id IS NOT NULL
    )
  );
ALTER TABLE
  commit
ADD
  CONSTRAINT fk_commit_parent FOREIGN KEY (parent_id) REFERENCES commit(commit_id);
ALTER TABLE
  commit_tree
ADD
  CONSTRAINT fk_commit_tree_commit FOREIGN KEY (commit_id) REFERENCES commit(commit_id);