# Thena Cockpit Client - Tenant Configuration Management

## Overview

The Thena Cockpit Client is a tenant configuration management system that provides a centralized way to organize, manage, and switch between different tenant configurations in multi-tenant applications. It implements the "one ring to rule them all" concept for tenant management.

## Problem Statement

In multi-tenant systems, managing configurations across numerous tenants with different requirements becomes complex. Organizations need:
- Visibility into tenant configurations without direct database access
- Ability to group related tenants together
- Runtime configuration switching based on request context
- Audit trail of configuration changes
- Flexible property management per tenant

## Core Concepts

### Tenant Configuration Groups
- **CockpitConfig**: Named configuration sets that group related tenants
- **Runtime Switching**: Ability to swap entire configuration groups based on request parameters
- **Centralized Management**: Single interface to manage all tenant configurations

### Tenant Types
The system supports various tenant types including:
- `git/single doc` - Document-based Git tenants
- `git/task` (mission control) - Task management tenants  
- `contract` - Contract management tenants
- `ledger` - Financial ledger tenants
- `batch` - Batch processing tenants
- `message queue` - Message processing tenants

### Commit Model Integration
Implements Git-like versioning for configuration changes:
- **Change Tracking**: Every configuration change is recorded with before/after states
- **Audit Trail**: Complete history of who changed what and when
- **Rollback Capability**: Ability to revert to previous configurations
- **Branching Support**: Multiple configuration branches per tenant

## Data Model

### Core Entities

#### CockpitConfig
Main configuration container that groups tenants and properties together.
```sql
cockpit_config
  id: uuid (PK)
  commit_id: uuid (FK)
  created_commit_id: uuid (FK)  
  updated_tree_commit_id: uuid (FK)
  external_id: text
  cockpit_config_name: text
  cockpit_config_desc: text
```

#### CockpitConfigTenant  
Individual tenant assignments within a configuration group.
```sql
cockpit_config_tenant
  id: uuid (PK)
  cockpit_config_id: uuid (FK)
  commit_id: uuid (FK)
  created_commit_id: uuid (FK)
  external_id: text
  external_branch: text
  cockpit_config_tenant_desc: text
  cockpit_config_tenant_extension: jsonb
```

#### CockpitConfigProps
Flexible properties that can be attached to configuration groups.
```sql
cockpit_config_props
  id: uuid (PK)
  cockpit_config_id: uuid (FK)
  commit_id: uuid (FK)
  created_commit_id: uuid (FK)
  cockpit_config_props_type: text
  cockpit_config_props_extension: jsonb
```

### Versioning System

#### CockpitConfigCommit
Git-like commit tracking for configuration changes.
```sql
cockpit_config_commit
  id: uuid (PK)
  parent_id: uuid (FK)
  created_at: timestamp
  commit_author: text
  commit_message: text
```

#### CockpitConfigCommitTree
Detailed change tracking with before/after states.
```sql
cockpit_config_commit_tree
  id: uuid (PK)
  commit_id: uuid (FK)
  operation_type: text (ADD, REMOVE, MERGE)
  body_before: jsonb
  body_after: jsonb
```

## Key Features

### 1. Configuration Grouping
- Group related tenants into named configurations
- Apply configuration changes to entire groups at once
- Manage tenant relationships and dependencies

### 2. Runtime Configuration Switching
- Switch between configuration groups based on request context
- Support for A/B testing and feature rollouts
- Seamless configuration transitions without downtime

### 3. Flexible Property Management
- JSONB extensions for custom properties
- Type-based property organization
- Tenant-specific overrides and customizations

### 4. Complete Audit Trail
- Track all configuration changes with timestamps and authors
- Before/after state comparison for changes
- Rollback capabilities for configuration recovery

### 5. Multi-Tenant Isolation
- Proper tenant data isolation using table name prefixing
- Secure tenant boundary enforcement
- Support for different tenant deployment patterns

## Architecture

### Generated Code Structure
Uses the Thena SQL annotation processor to generate:
- **Entity Classes**: Immutable data objects with Jackson serialization
- **Table Interfaces**: Type-safe database access with CRUD operations  
- **Repository Implementations**: Auto-generated SQL execution code
- **Query Builders**: Flexible filtering and search capabilities

### Entity Pattern
All entities follow the standard Thena pattern:
- `@Value.Immutable` for immutable data objects
- `CockpitEntity` base interface with common operations
- `CockpitDocType` enum for polymorphic type identification
- Jackson annotations for JSON serialization

### Table Pattern
All tables implement the Thena SQL pattern:
- `@TenantSql.Table` annotations with DDL, constraints, and drop statements
- Tenant-aware table name placeholders (`{table_name}`)
- Type-safe mapper classes for DB ” entity conversion
- Batch operations support for performance

## Usage Scenarios

### 1. Environment Management
```java
// Switch all tenants to staging configuration
CockpitConfig stagingConfig = cockpitService.getConfigByName("staging-env");
cockpitService.activateConfiguration(stagingConfig.getId());
```

### 2. Feature Rollout
```java
// Gradually move tenants to new feature configuration
CockpitConfig newFeatureConfig = cockpitService.createConfig("feature-rollout-v2");
newFeatureConfig.addTenant("tenant-pilot-1");
newFeatureConfig.addTenant("tenant-pilot-2");
cockpitService.saveConfiguration(newFeatureConfig);
```

### 3. Tenant Onboarding
```java
// Add new tenant to existing configuration group
CockpitConfigTenant newTenant = CockpitConfigTenant.builder()
    .externalId("new-customer-123")
    .externalBranch("production")
    .cockpitConfigTenantDesc("New enterprise customer")
    .build();
    
cockpitService.addTenantToConfig(productionConfigId, newTenant);
```

### 4. Configuration Audit
```java
// View change history for a configuration
List<CockpitConfigCommit> history = cockpitService.getConfigHistory(configId);
for(CockpitConfigCommit commit : history) {
    System.out.println("Change by: " + commit.getCommitAuthor());
    System.out.println("Message: " + commit.getCommitMessage());
    
    // View detailed changes
    List<CockpitConfigCommitTree> changes = cockpitService.getCommitChanges(commit.getId());
    // Analyze before/after states
}
```

## Implementation Notes

### Naming Conventions
- **Tables**: snake_case (e.g., `cockpit_config_tenant`)
- **Fields**: table_name_field_name pattern with exceptions for `external_id` and `external_branch`
- **Entities**: PascalCase (e.g., `CockpitConfigTenant`)
- **Primary Keys**: Always named `id`

### Foreign Key Relationships
- All FKs reference commit tables for proper versioning
- Cascade deletes handled through explicit SQL constraints
- Indexes on all FK columns for query performance

### JSONB Extensions
- Flexible schema evolution without migration overhead
- Support for complex nested property structures
- Efficient querying using PostgreSQL JSONB operators

### Commit Model Benefits
- **Auditability**: Complete change tracking for compliance
- **Rollback**: Safe configuration recovery mechanisms  
- **Branching**: Support for parallel configuration development
- **Merge**: Ability to combine configuration changes

## Development Workflow

1. **Entity Design**: Define new entity interfaces following Thena patterns
2. **Table Definition**: Create table interfaces with proper annotations
3. **Code Generation**: Let annotation processor generate implementations
4. **Service Layer**: Build business logic on top of generated repositories
5. **Testing**: Use generated code for integration testing

This architecture provides a robust, scalable foundation for managing complex multi-tenant configurations while maintaining data integrity, audit trails, and operational flexibility.