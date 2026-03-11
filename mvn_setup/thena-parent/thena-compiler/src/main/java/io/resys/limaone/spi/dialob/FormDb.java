package io.resys.limaone.spi.dialob;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Budget multi-tenant form database interface for Dialob integration.
 * 
 * <p>Provides a "cheap tenancy" solution using database field-level tenant isolation
 * rather than full tenant systems. Designed to work with emotionally damaged coworkers
 * and their existing APIs that never quite became proper APIs.
 * 
 * <p>Integrates with Dialob form management through the reactive HTTP client,
 * providing form CRUD operations, versioning via FormTags, and lightweight metadata queries.
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * // Multi-operation tenant session
 * FormTenant tenant = formDb.withTenant("customer-123");
 * 
 * // Query forms
 * Form form = tenant.formQuery()
 *   .formTag("registration-form", "v1.0.79_preprod")
 *   .findOne()
 *   .await().indefinitely().orElse(null);
 * 
 * // Create new form version
 * FormTag tag = tenant.createFormTag()
 *   .formId("form-abc123")
 *   .formVersion("v1.0.80_prod")
 *   .build()
 *   .await().indefinitely();
 * }</pre>
 * 
 * <h3>Performance Notes:</h3>
 * <ul>
 *   <li>Forms can be heavy (1MB+) - use FormMetaQuery for headers only</li>
 *   <li>FormTagQuery.findAll() can cause latency issues with many forms/tags</li>
 *   <li>All operations are "reactive" (using HTTP client wrapper)</li>
 * </ul>
 * 
 * @since 1.0
 */
public interface FormDb {
  
  /**
   * Gets the default tenant name configured for this FormDb instance.
   * 
   * @return the default tenant name loaded at construction time
   */
  String getTenantName();
  
  /**
   * Creates a tenant session using the default tenant configured at construction.
   * 
   * @return a {@link FormTenant} for the default tenant
   */
  FormTenant withTenant();
  
  /**
   * Creates a tenant session for the specified tenant identifier.
   * Accepts variety of tenant identifiers: human readable names, external IDs, technical IDs.
   * 
   * @param tenantIdOrName the tenant identifier (name, external ID, or technical ID)
   * @return a {@link FormTenant} for the specified tenant
   */
  FormTenant withTenant(String tenantIdOrName);
  
  /**
   * Tenant-scoped form operations interface.
   * Designed for multi-operation usage - load the tenant and query/write until the cows come home.
   * Provides isolated access to forms within a specific tenant context.
   */
  interface FormTenant {
    
    /**
     * Gets the resolved tenant ID for this tenant session.
     * 
     * @return the technical tenant ID
     */
    String getTenantId();
    
    /**
     * Creates a form query builder for finding forms by ID or tag.
     * 
     * @return a new {@link FormQuery} builder
     */
    FormQuery formQuery();
    
    /**
     * Creates a form tag query for listing all tags in this tenant.
     * <strong>Warning:</strong> Can cause latency issues with many forms (100 forms × 10 tags = brain-out operation).
     * 
     * @return a new {@link FormTagQuery} builder
     */
    FormTagQuery formTagQuery();
    
    /**
     * Creates a form metadata query for lightweight form headers without full content.
     * Use this instead of full form queries when you don't need the heavy form content (1MB+).
     * 
     * @return a new {@link FormMetaQuery} builder
     */
    FormMetaQuery formMetaQuery();
    
    /**
     * Creates a builder for creating brand new forms.
     * Semantic difference from merge - use this for completely new forms.
     * 
     * @return a new {@link CreateForm} builder
     */
    CreateForm createForm();
    
    /**
     * Creates a builder for creating form tags (versions) on existing forms.
     * 
     * @return a new {@link CreateFormTag} builder
     */
    CreateFormTag createFormTag();
    
    /**
     * Creates a builder for merging/updating existing forms.
     * Semantic difference from create - use this for updating forms that already exist.
     * 
     * @return a new {@link MergeForm} builder
     */
    MergeForm mergeForm();
  }
  
  /**
   * Query builder for finding forms by technical ID or human-readable name/version.
   * Supports alternative query methods - either by technical form ID or by form name + version tag.
   */
  interface FormQuery {
    
    /**
     * Query by technical form ID (like accessing specific tag by commit ID).
     * This might be misleading but kind of not - it's direct technical access.
     * 
     * @param formId the technical form identifier
     * @return this query builder for method chaining
     */
    FormQuery formId(String formId);
    
    /**
     * Query by human-readable form name and version tag.
     * Example: formTag("registration-form", "v1.0.79_preprod")
     * 
     * @param formName the human-readable form name
     * @param formVersion the human-readable version like "v1.0.79_preprod"
     * @return this query builder for method chaining
     */
    FormQuery formTag(String formName, String formVersion);
    
    /**
     * Executes the query and returns an optional form.
     * Returns empty Optional for missing forms (no exceptions thrown).
     * 
     * @return a {@link Uni} containing an Optional form (empty if not found)
     */
    Uni<Optional<Form>> findOne();
  }
  
  /**
   * Query builder for lightweight form metadata without heavy form content.
   * Use this when you need form headers but don't want the full 1MB+ form content.
   */
  interface FormMetaQuery {
    
    /**
     * Finds all form metadata in the current tenant.
     * Returns lightweight headers without the heavy form content.
     * 
     * @return a {@link Multi} stream of form metadata objects
     */
    Multi<FormMetadata> findAll();
  }
  
  /**
   * Query builder for form tags across all forms in the tenant.
   * <strong>Performance Warning:</strong> Brain-out operation with many forms.
   */
  interface FormTagQuery {
    
    /**
     * Finds all form tags across all forms in the current tenant.
     * <strong>Warning:</strong> Can cause serious latency with many forms/tags.
     * 
     * @return a list of all form tags in the tenant
     */
    List<FormTag> findAll(); 
  }
  
  /**
   * Builder for creating brand new forms.
   * Semantic API - use this for completely new forms, not updates.
   */
  interface CreateForm {
    
    /**
     * Sets the form to be created.
     * 
     * @param form the form object to create
     * @return this builder for method chaining
     */
    CreateForm props(Form form);
    
    /**
     * Executes the form creation.
     * 
     * @return a {@link Uni} containing the created form with assigned ID
     */
    Uni<Form> build();
  }
  
  /**
   * Builder for merging/updating existing forms.
   * Semantic API - use this for updating forms that already exist.
   */
  interface MergeForm {
    
    /**
     * Sets the form to be merged/updated.
     * 
     * @param form the form object to merge (should have existing ID)
     * @return this builder for method chaining
     */
    MergeForm props(Form form);
    
    /**
     * Executes the form merge/update.
     * 
     * @return a {@link Uni} containing the updated form
     */
    Uni<Form> build();
  }
  
  /**
   * Builder for creating form tags (versions) on existing forms.
   * Form tags provide versioning and environment management (dev/prod/preprod).
   */
  interface CreateFormTag {
    
    /**
     * Sets the technical form ID to tag.
     * 
     * @param formId the technical form identifier
     * @return this builder for method chaining
     */
    CreateFormTag formId(String formId);
    
    /**
     * Sets the human-readable version name for the tag.
     * Example: "v1.0.79_preprod", "v2.0.1_prod"
     * 
     * @param tagName the human-readable version/tag name
     * @return this builder for method chaining
     */
    CreateFormTag formVersion(String tagName);
    
    /**
     * Executes the form tag creation.
     * 
     * @return a {@link Uni} containing the created form tag with technical ID
     */
    Uni<FormTag> build();
  }
  
  /**
   * Lightweight form metadata without heavy form content.
   * Use this when you need form information but don't want the full 1MB+ form data.
   */
  @Value.Immutable @JsonSerialize(as = ImmutableFormMetadata.class) @JsonDeserialize(as = ImmutableFormMetadata.class)
  interface FormMetadata {
    
    /**
     * Gets the technical form ID.
     * 
     * @return the form's technical identifier
     */
    String getId();
    
    /**
     * Gets the form metadata headers without the heavy content.
     * 
     * @return the form's metadata object
     */
    Form.Metadata getMetadata();   
  }
}
