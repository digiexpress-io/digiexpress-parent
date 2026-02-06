package io.resys.thena.fs.api;

/*-
 * #%L
 * thena-contract-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.fs.api.blobs.BlobQuery;
import io.resys.thena.fs.api.branches.BranchBuilder;
import io.resys.thena.fs.api.branches.BranchQuery;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.commits.CommitQuery;
import io.resys.thena.fs.api.tags.TagBuilder;
import io.resys.thena.fs.api.tags.TagQuery;

/**
 * Main entry point for Git-like filesystem operations with multi-tenant support.
 * Provides a versioned, content-addressable filesystem with commit history,
 * branching, and tagging capabilities backed by PostgreSQL.
 */
public interface FileSystem {
  /**
   * Access tenant management operations for creating and managing isolated
   * filesystem instances. Each tenant maintains its own independent namespace
   * with separate commits, branches, and file storage.
   * 
   * @return tenant actions for managing filesystem tenants
   */
  TenantActions tenants();
  
  /**
   * Creates a filesystem context for the default tenant.
   * Uses system-configured default tenant settings.
   * 
   * @return tenant-scoped filesystem operations
   */
  FileSystemTenant withTenant();
  
  /**
   * Creates a filesystem context for a specific tenant by ID or name.
   * Enables operations within that tenant's isolated filesystem namespace.
   * 
   * @param tenantIdOrName the tenant identifier or human-readable name
   * @return tenant-scoped filesystem operations
   */
  FileSystemTenant withTenant(String tenantIdOrName);
  
  /**
   * Creates a filesystem context for a newly created tenant.
   * Typically used immediately after tenant creation operations.
   * 
   * @param repo the created tenant reference
   * @return tenant-scoped filesystem operations
   */
  FileSystemTenant withTenant(CreatedTenant repo);
  
  /**
   * Creates a filesystem context for an existing tenant entity.
   * Provides access to operations within that tenant's filesystem namespace.
   * 
   * @param repo the tenant entity
   * @return tenant-scoped filesystem operations
   */
  FileSystemTenant withTenant(Tenant repo);

  /**
   * Tenant-scoped filesystem interface providing access to all Git-like operations
   * within an isolated tenant namespace. Each tenant maintains independent 
   * version history, branches, and file storage.
   */
  interface FileSystemTenant {
    /**
     * @return the unique identifier of the loaded tenant context
     */
    String getTenantId();
    
    /**
     * Creates a new commit builder for atomically modifying the filesystem.
     * Supports batching multiple file/folder operations into a single commit
     * with transactional semantics to prevent race conditions.
     * 
     * @return builder for creating filesystem commits
     */
    CommitBuilder commitBuilder();
    
    /**
     * Creates a query interface for searching commit history and tracking
     * how files and folders evolved over time. Supports filtering by branch,
     * path patterns, and excluding data types for performance optimization.
     * 
     * @return query interface for commit history
     */
    CommitQuery commitQuery();
    
    /**
     * Creates a builder for creating tags that mark specific commits.
     * Supports rich metadata, external system integration, and workflow
     * automation with scheduling and error tracking capabilities.
     * 
     * @return builder for creating tags
     */
    TagBuilder tagBuilder();
    
    /**
     * Creates a query interface for retrieving tags with configurable
     * content loading. Supports filtering by tag properties and controlling
     * the depth of related data to optimize performance.
     * 
     * @return query interface for tags
     */
    TagQuery tagQuery();
   
    /**
     * Creates a builder for managing branch references with metadata.
     * Supports branch creation with descriptions, access controls, UI
     * customization, and workflow configuration.
     * 
     * @return builder for managing branches
     */
    BranchBuilder branchBuilder();
    
    /**
     * Creates a query interface for retrieving branch references.
     * Supports filtering by name patterns and accessing branch metadata
     * including permissions, configuration, and ownership information.
     * 
     * @return query interface for branches
     */
    BranchQuery branchQuery();
    
    /**
     * Creates a query interface for retrieving file content with filtering.
     * Supports querying by branch state, file properties, and path patterns
     * with performance optimization through content exclusion controls.
     * 
     * @return query interface for file blobs
     */
    BlobQuery blobQuery();
  }

}
