-- Contract System Schema
-- Generated with expanded business date fields

-- Commit table (versioning support)
CREATE TABLE {commit} (
  commit_id UUID PRIMARY KEY,
  parent_id UUID,
  contract_id UUID,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  commit_log TEXT NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);

CREATE INDEX {commit}_PARENT_INDEX ON {commit} (parent_id);
CREATE INDEX {commit}_CONTRACT_INDEX ON {commit} (contract_id);
CREATE INDEX {commit}_AUTH_INDEX ON {commit} (commit_author);

-- Commit tree table (operation logging)
CREATE TABLE {commit_tree} (
  id UUID PRIMARY KEY,
  commit_id UUID NOT NULL,
  operation_type VARCHAR(40),
  body_after JSONB,
  body_before JSONB
);

CREATE INDEX {commit_tree}_COMMIT_INDEX ON {commit_tree} (commit_id);

-- Self-referencing FK for commit table
ALTER TABLE {commit}
  ADD CONSTRAINT {commit}_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES {commit} (commit_id);

-- Contract table
CREATE TABLE {contract} (
    id                              UUID PRIMARY KEY,
    parent_contract_id              UUID,
    contract_number                 VARCHAR(255) NOT NULL,
    
    external_id                     VARCHAR(255),
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    updated_tree_commit_id          UUID NOT NULL,
    
    contract_issue_date             DATE NOT NULL,
    contract_issue_date_interval    INTERVAL NOT NULL,
    contract_issue_date_type        VARCHAR(100) NOT NULL,
    
    contract_start_date             DATE NOT NULL,
    contract_start_date_interval    INTERVAL NOT NULL,
    contract_start_date_type        VARCHAR(100) NOT NULL,
    
    contract_maturity_date          DATE,
    contract_maturity_date_interval INTERVAL,
    contract_maturity_date_type     VARCHAR(100),
    
    contract_status                 VARCHAR(100) NOT NULL,
    contract_sub_status             VARCHAR(100),
    contract_type                   VARCHAR(100) NOT NULL,
    contract_sub_type               VARCHAR(100),
    contract_data                   JSONB
);

-- Party table
CREATE TABLE {party} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    
    external_id                     VARCHAR(255) NOT NULL,
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    party_type                      VARCHAR(100) NOT NULL,
    party_effective_from            DATE NOT NULL,
    party_effective_to              DATE,
    
    party_term_start_date           DATE NOT NULL,
    party_term_start_date_interval  INTERVAL NOT NULL,
    party_term_start_date_type      VARCHAR(100) NOT NULL,
    
    party_term_end_date             DATE,
    party_term_end_date_interval    INTERVAL,
    party_term_end_date_type        VARCHAR(100),
    
    party_data                      JSONB
);

-- Coverage table
CREATE TABLE {coverage} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    insured_id                      UUID NOT NULL,
    
    external_id                     VARCHAR(255) NOT NULL,
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    coverage_type                   VARCHAR(100) NOT NULL,
    coverage_code                   VARCHAR(100) NOT NULL,
    coverage_sum_insured            DECIMAL(15,2),
    coverage_rate                   DECIMAL(10,6),
    coverage_rate_type              VARCHAR(100),
    coverage_status                 VARCHAR(100) NOT NULL,
    coverage_effective_from         DATE NOT NULL,
    coverage_effective_to           DATE,
    
    coverage_term_start_date        DATE NOT NULL,
    coverage_term_start_date_interval INTERVAL NOT NULL,
    coverage_term_start_date_type   VARCHAR(100) NOT NULL,
    
    coverage_term_end_date          DATE,
    coverage_term_end_date_interval INTERVAL,
    coverage_term_end_date_type     VARCHAR(100)
);

-- Capability table
CREATE TABLE {capability} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    
    external_id                     VARCHAR(255),
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    capability_code                 VARCHAR(100) NOT NULL,
    capability_name                 VARCHAR(255) NOT NULL,
    capability_type                 VARCHAR(100) NOT NULL,
    capability_enabled              BOOLEAN NOT NULL DEFAULT TRUE
);

-- Reference table
CREATE TABLE {reference} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    inv_plan_id                     UUID,
    inv_plan_alloc_id               UUID,
    coverage_id                     UUID,
    party_id                        UUID,
    
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    reference_value                 VARCHAR(500) NOT NULL,
    reference_type                  VARCHAR(100) NOT NULL,
    reference_body                  JSONB
);

-- Note table
CREATE TABLE {note} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    inv_plan_id                     UUID,
    inv_plan_alloc_id               UUID,
    coverage_id                     UUID,
    party_id                        UUID,
    
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    note_value                      VARCHAR(500) NOT NULL,
    note_type                       VARCHAR(100) NOT NULL,
    note_body                       JSONB
);

-- Payment Plan table
CREATE TABLE {payment_plan} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    party_id                        UUID,
    
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    payment_plan_frequency          VARCHAR(100) NOT NULL,
    payment_plan_day                INTEGER NOT NULL,
    payment_plan_amount             DECIMAL(15,2) NOT NULL,
    payment_plan_effective_from     DATE NOT NULL,
    payment_plan_effective_to       DATE,
    
    payment_plan_term_start_date    DATE NOT NULL,
    payment_plan_term_start_date_interval INTERVAL NOT NULL,
    payment_plan_term_start_date_type VARCHAR(100) NOT NULL,
    
    payment_plan_term_end_date      DATE,
    payment_plan_term_end_date_interval INTERVAL,
    payment_plan_term_end_date_type VARCHAR(100),
    
    payment_plan_data               JSONB
);

-- Investment Plan table
CREATE TABLE {inv_plan} (
    id                              UUID PRIMARY KEY,
    contract_id                     UUID NOT NULL,
    
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    inv_plan_ref                    VARCHAR(255) NOT NULL,
    inv_plan_type                   VARCHAR(100) NOT NULL,
    inv_plan_status                 VARCHAR(100) NOT NULL,
    inv_plan_effective_from         DATE NOT NULL,
    inv_plan_effective_to           DATE,
    
    inv_plan_term_start_date        DATE NOT NULL,
    inv_plan_term_start_date_interval INTERVAL NOT NULL,
    inv_plan_term_start_date_type   VARCHAR(100) NOT NULL,
    
    inv_plan_term_end_date          DATE,
    inv_plan_term_end_date_interval INTERVAL,
    inv_plan_term_end_date_type     VARCHAR(100),
    
    inv_plan_data                   JSONB
);

-- Investment Plan Allocation table
CREATE TABLE {inv_plan_alloc} (
    id                              UUID PRIMARY KEY,
    inv_plan_id                     UUID NOT NULL,
    
    commit_id                       UUID NOT NULL,
    created_commit_id               UUID NOT NULL,
    
    investment_id                   VARCHAR(255) NOT NULL,
    inv_plan_alloc_percentage       DECIMAL(5,2) NOT NULL,
    inv_plan_alloc_data             JSONB
);

-- Foreign Key Constraints
ALTER TABLE {contract} ADD CONSTRAINT fk_contract_parent 
    FOREIGN KEY (parent_contract_id) REFERENCES {contract}(id);

ALTER TABLE {party} ADD CONSTRAINT fk_party_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);

ALTER TABLE {coverage} ADD CONSTRAINT fk_coverage_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);
ALTER TABLE {coverage} ADD CONSTRAINT fk_coverage_insured 
    FOREIGN KEY (insured_id) REFERENCES {party}(id);

ALTER TABLE {capability} ADD CONSTRAINT fk_capability_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);

ALTER TABLE {reference} ADD CONSTRAINT fk_reference_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);
ALTER TABLE {reference} ADD CONSTRAINT fk_reference_inv_plan 
    FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
ALTER TABLE {reference} ADD CONSTRAINT fk_reference_inv_plan_alloc 
    FOREIGN KEY (inv_plan_alloc_id) REFERENCES {inv_plan_alloc}(id);
ALTER TABLE {reference} ADD CONSTRAINT fk_reference_coverage 
    FOREIGN KEY (coverage_id) REFERENCES {coverage}(id);
ALTER TABLE {reference} ADD CONSTRAINT fk_reference_party 
    FOREIGN KEY (party_id) REFERENCES {party}(id);

ALTER TABLE {note} ADD CONSTRAINT fk_note_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);
ALTER TABLE {note} ADD CONSTRAINT fk_note_inv_plan 
    FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
ALTER TABLE {note} ADD CONSTRAINT fk_note_inv_plan_alloc 
    FOREIGN KEY (inv_plan_alloc_id) REFERENCES {inv_plan_alloc}(id);
ALTER TABLE {note} ADD CONSTRAINT fk_note_coverage 
    FOREIGN KEY (coverage_id) REFERENCES {coverage}(id);
ALTER TABLE {note} ADD CONSTRAINT fk_note_party 
    FOREIGN KEY (party_id) REFERENCES {party}(id);

ALTER TABLE {payment_plan} ADD CONSTRAINT fk_payment_plan_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);
ALTER TABLE {payment_plan} ADD CONSTRAINT fk_payment_plan_party 
    FOREIGN KEY (party_id) REFERENCES {party}(id);

ALTER TABLE {inv_plan} ADD CONSTRAINT fk_inv_plan_contract 
    FOREIGN KEY (contract_id) REFERENCES {contract}(id);

ALTER TABLE {inv_plan_alloc} ADD CONSTRAINT fk_inv_plan_alloc_inv_plan 
    FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);

-- Commit FK constraints
ALTER TABLE {contract} ADD CONSTRAINT fk_contract_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {contract} ADD CONSTRAINT fk_contract_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {contract} ADD CONSTRAINT fk_contract_updated_tree_commit 
    FOREIGN KEY (updated_tree_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {party} ADD CONSTRAINT fk_party_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {party} ADD CONSTRAINT fk_party_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {coverage} ADD CONSTRAINT fk_coverage_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {coverage} ADD CONSTRAINT fk_coverage_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {capability} ADD CONSTRAINT fk_capability_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {capability} ADD CONSTRAINT fk_capability_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {reference} ADD CONSTRAINT fk_reference_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {reference} ADD CONSTRAINT fk_reference_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {note} ADD CONSTRAINT fk_note_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {note} ADD CONSTRAINT fk_note_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {payment_plan} ADD CONSTRAINT fk_payment_plan_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {payment_plan} ADD CONSTRAINT fk_payment_plan_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {inv_plan} ADD CONSTRAINT fk_inv_plan_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {inv_plan} ADD CONSTRAINT fk_inv_plan_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

ALTER TABLE {inv_plan_alloc} ADD CONSTRAINT fk_inv_plan_alloc_commit 
    FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
ALTER TABLE {inv_plan_alloc} ADD CONSTRAINT fk_inv_plan_alloc_created_commit 
    FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);

-- Indexes
CREATE INDEX idx_contract_status ON {contract}(contract_status);
CREATE INDEX idx_contract_type ON {contract}(contract_type);
CREATE INDEX idx_contract_sub_status ON {contract}(contract_sub_status);
CREATE INDEX idx_contract_sub_type ON {contract}(contract_sub_type);
CREATE INDEX idx_contract_parent ON {contract}(parent_contract_id);
CREATE INDEX idx_contract_commit ON {contract}(commit_id);
CREATE INDEX idx_contract_created_commit ON {contract}(created_commit_id);
CREATE INDEX idx_contract_updated_tree_commit ON {contract}(updated_tree_commit_id);

CREATE INDEX idx_party_type ON {party}(party_type);
CREATE INDEX idx_party_contract ON {party}(contract_id);
CREATE INDEX idx_party_commit ON {party}(commit_id);
CREATE INDEX idx_party_created_commit ON {party}(created_commit_id);

CREATE INDEX idx_coverage_type ON {coverage}(coverage_type);
CREATE INDEX idx_coverage_status ON {coverage}(coverage_status);
CREATE INDEX idx_coverage_contract ON {coverage}(contract_id);
CREATE INDEX idx_coverage_insured ON {coverage}(insured_id);
CREATE INDEX idx_coverage_commit ON {coverage}(commit_id);
CREATE INDEX idx_coverage_created_commit ON {coverage}(created_commit_id);

CREATE INDEX idx_capability_type ON {capability}(capability_type);
CREATE INDEX idx_capability_contract ON {capability}(contract_id);
CREATE INDEX idx_capability_commit ON {capability}(commit_id);
CREATE INDEX idx_capability_created_commit ON {capability}(created_commit_id);

CREATE INDEX idx_reference_type ON {reference}(reference_type);
CREATE INDEX idx_reference_contract ON {reference}(contract_id);
CREATE INDEX idx_reference_inv_plan ON {reference}(inv_plan_id);
CREATE INDEX idx_reference_inv_plan_alloc ON {reference}(inv_plan_alloc_id);
CREATE INDEX idx_reference_coverage ON {reference}(coverage_id);
CREATE INDEX idx_reference_party ON {reference}(party_id);
CREATE INDEX idx_reference_commit ON {reference}(commit_id);
CREATE INDEX idx_reference_created_commit ON {reference}(created_commit_id);

CREATE INDEX idx_note_type ON {note}(note_type);
CREATE INDEX idx_note_contract ON {note}(contract_id);
CREATE INDEX idx_note_inv_plan ON {note}(inv_plan_id);
CREATE INDEX idx_note_inv_plan_alloc ON {note}(inv_plan_alloc_id);
CREATE INDEX idx_note_coverage ON {note}(coverage_id);
CREATE INDEX idx_note_party ON {note}(party_id);
CREATE INDEX idx_note_commit ON {note}(commit_id);
CREATE INDEX idx_note_created_commit ON {note}(created_commit_id);

CREATE INDEX idx_payment_plan_contract ON {payment_plan}(contract_id);
CREATE INDEX idx_payment_plan_party ON {payment_plan}(party_id);
CREATE INDEX idx_payment_plan_commit ON {payment_plan}(commit_id);
CREATE INDEX idx_payment_plan_created_commit ON {payment_plan}(created_commit_id);

CREATE INDEX idx_inv_plan_type ON {inv_plan}(inv_plan_type);
CREATE INDEX idx_inv_plan_status ON {inv_plan}(inv_plan_status);
CREATE INDEX idx_inv_plan_contract ON {inv_plan}(contract_id);
CREATE INDEX idx_inv_plan_commit ON {inv_plan}(commit_id);
CREATE INDEX idx_inv_plan_created_commit ON {inv_plan}(created_commit_id);

CREATE INDEX idx_inv_plan_alloc_inv_plan ON {inv_plan_alloc}(inv_plan_id);
CREATE INDEX idx_inv_plan_alloc_commit ON {inv_plan_alloc}(commit_id);
CREATE INDEX idx_inv_plan_alloc_created_commit ON {inv_plan_alloc}(created_commit_id);