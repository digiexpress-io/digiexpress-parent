package io.resys.limaone.spi.dialob;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormTag;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.rest.IdAndRevision;
import io.resys.limaone.spi.http.HttpClient.AnyProxy;
import io.resys.limaone.spi.http.HttpClient.RawResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

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
    
    /**
     * Creates a builder for merging/updating existing form instances.
     * Used to apply form filling events (user actions) to questionnaire instances,
     * such as field updates, form completion, and navigation actions.
     * 
     * @return a new {@link MergeFormInstance} builder
     */
    MergeFormInstance mergeFormInstance();
    
    /**
     * Creates a query builder for retrieving form instances from the database.
     * Supports optional form data enrichment with valueset and FormItem metadata.
     * 
     * @return a new {@link FormInstanceQuery} builder
     */
    FormInstanceQuery formInstanceQuery();
    
    /**
     * Creates a builder for instantiating new questionnaires from form templates.
     * Supports pre-population with context variables and initial answers.
     * 
     * @return a new {@link CreateFormInstance} builder
     */
    CreateFormInstance createFormInstance();
    
    /**
     * Creates a low-level HTTP proxy builder for form filling operations.
     * Use this for performance-critical scenarios requiring direct HTTP passthrough
     * with raw payloads and fire-and-forget post-processing capabilities.
     * 
     * @return a new {@link FormFillBuilder} for low-level form operations
     */
    FormFillBuilder createFormFill();
    
    /**
     * Creates a low-level HTTP proxy query for retrieving form instances.
     * Use this for direct access to raw HTTP responses without domain object
     * marshalling overhead in performance-critical scenarios.
     * 
     * @return a new {@link FormFillQuery} for low-level form queries
     */
    FormFillQuery formFillQuery();

    /**
     * Creates a low-level HTTP proxy query for the flat data document of a
     * COMPLETED questionnaire (dialob: GET /questionnaires/{id}/session-state).
     * Raw JSON passthrough intended for printout/PDF template pipelines.
     * Supports the same language and timezone overrides as dialob's
     * getQuestionnaireSessionState ({@code lang} and {@code tz} query parameters).
     *
     * @return a new {@link FormInstanceFlatDataQuery} for flat data queries
     */
    FormInstanceFlatDataQuery formInstanceFlatDataQuery();

    /**
     * Creates a read-only form state reconstruction engine for displaying completed forms.
     * Provides in-memory simulation of completed questionnaires with navigation support
     * to show form state at any page or step without affecting the original data.
     * All errors are hidden.
     * 
     * @return a new {@link FormFillReview} for read-only form state reconstruction
     */
    FormFillReview formFillReview();
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
    
    
    /**
     * Executes the query and returns all forms.
     * 
     * @return a {@link Multi} stream of form form objects 
     */
    Multi<Form> findAll();
    
    /**
     * @return create fluent builder to proxy anything to what's in the current implementation
     */
    AnyProxy proxyAnything();
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
     * Finds all form tags for a specific form.
     * Returns tags for a single form by its 'lexical name'.
     * 
     * @param formId the technical form identifier
     * @return a {@link Multi} stream of form tags for the specified form
     */
    Multi<FormTag> findAll(String formName);
    
    /**
     * Gets a specific form tag by form 'lexical name' and tag name.
     * Direct access to a specific tagged version.
     * 
     * @param formName the 'lexical name' form identifier
     * @param tagName the tag/version name
     * @return a {@link Uni} containing the specific form tag
     */
    Uni<FormTag> getOneTag(String formName, String tagName);
    
    /**
     * Finds all form tags across all forms in the current tenant.
     * <strong>Warning:</strong> Can cause serious latency with many forms/tags.
     * 
     * @return a {@link Multi} stream of all form tags
     */
    Multi<FormTag> findAll(); 
    
    
    /**
     * Finds all forms AND tags across current tenant.
     * <strong>Warning:</strong> Can cause serious latency with many forms/tags.
     * 
     * @return a {@link Multi} stream of all forms tags
     */
    Multi<FormAndTag> flatAll();
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
     * Sets the form 'lexical name' to tag.
     * 
     * @param formName the 'lexical name' form identifier
     * @return this builder for method chaining
     */
    CreateFormTag formName(String formName);
    
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
  
  
  /**
   * Wrapper around Dialob's Questionnaire providing additional metadata and state.
   * Will later be extended to provide more contextual data around the questionnaire instance.
   */
  interface FormInstance {
    /**
     * Gets the underlying Dialob form.
     * 
     * @return the form for the questionnaire.
     */
    Optional<Form> getForm();
    
    /**
     * Gets the underlying Dialob questionnaire containing form state and responses.
     * When form data is loaded, the questionnaire contains enriched data merged from 
     * the form definition including valuesets and FormItems metadata.
     * 
     * @return the questionnaire instance with current form state
     */
    Questionnaire getQuestionnaire();
    
    /**
     * Retrieves the raw Answer and FormItem object for the whole Questionnaire.
     * 
     * @return the Answer object AND FormItem for the whole Questionnaire.
     */
    List<AnswerAndFormItem> getAnswerAndFormItems();

    /**
     * Retrieves the raw Answer object for a given field name.
     * Searches through questionnaire answers by ID match.
     * 
     * @param name the field name/ID to retrieve the answer for
     * @return the Answer object, or null if field not found
     */
    Answer answer(String name);
    
    /**
     * Retrieves the string value of a form field answer.
     * Safely handles null answers and null values by returning empty string.
     * 
     * @param name the field name to retrieve the text value for
     * @return the string value converted via toString(), or empty string ("") if answer/value is null or field not found
     */
    String text(String name);
    
    /**
     * Retrieves the numeric value of a decimal/integer field answer.
     * Expects answer type to be "INTEGER" or "DECIMAL" (assertion will fail otherwise).
     * Safely handles null answers and null values by returning zero.
     * 
     * @param name the field name to retrieve the decimal value for
     * @return the BigDecimal value parsed from answer.getValue().toString(), or BigDecimal.ZERO if answer/value is null or field not found
     * @throws AssertionError if answer exists but type is not "INTEGER" or "DECIMAL"
     */
    BigDecimal decimal(String name);
    
    /**
     * Retrieves the boolean value of a boolean field answer.
     * Expects answer type to be "BOOLEAN" (assertion will fail otherwise).
     * Safely handles null answers and null values by returning false.
     * 
     * @param name the field name to retrieve the boolean value for
     * @return true if answer value equals Boolean.TRUE, false if answer/value is null, field not found, or value is not Boolean.TRUE
     * @throws AssertionError if answer exists but type is not "BOOLEAN"
     */
    boolean bool(String name);
    
    /**
     * Retrieves a form variable value by name.
     * Variables are calculated fields or internal form state values.
     * Searches through questionnaire variable values by ID match.
     * 
     * @param name the variable name/ID to retrieve
     * @return the variable value from VariableValue.getValue(), or null if variable not found
     * @throws NullPointerException if matching variable is found but has null value
     */
    Object variable(String name);
    
    /**
     * Retrieves a context variable value by name.
     * Context variables are predefined values set during form instantiation.
     * Searches through questionnaire context values by ID match.
     * 
     * @param name the context variable name/ID to retrieve
     * @return the context value from ContextValue.getValue(), or null if context variable not found
     */
    Object context(String name);

    
    /**
     * Gets the prefix used for valueset lookup entries.
     * Used to identify dynamically generated lookup valuesets for answer metadata.
     * 
     * @return the lookup prefix, or empty if form data was not loaded
     */
    Optional<String> getLookupPrefix();

    /**
     * Indicates whether form data has been loaded and merged into the questionnaire.
     * When true, the questionnaire contains enriched data from the form definition
     * including valuesets and FormItems metadata merged with answer data.
     * 
     * @return true if form data is loaded and merged, false otherwise
     */
    boolean isFormLoaded();
    
    /**
     * Retrieves the FormItem metadata for a given answer.
     * Provides access to form field definitions and metadata associated with the answer.
     * Handles both regular fields and rowgroup elements with dot notation IDs.
     * 
     * @param answer the answer to get FormItem metadata for
     * @return the FormItem metadata, or null if not found or form data not loaded
     */
    FormItem getFormItem(Answer answer);
    
    /**
     * @return pretty print format the form when its loaded
     */
    Optional<String> encodeFormPrettily();
    
    /**
     * @return shortcut method to questionnaire attribute
     */
    default Questionnaire.Metadata metadata() {
      return this.getQuestionnaire().metadata();
    }
  }
  
  /**
   * Builder for applying form filling events to existing form instances.
   * Merges actions (user input events) on top of the form instance state,
   * such as field updates, completions, and other form interactions.
   */
  interface MergeFormInstance {
    
    /**
     * Specifies the target form instance to merge actions into.
     * 
     * @param questionnaireId the unique identifier of the questionnaire instance
     * @return this builder for method chaining
     */
    MergeFormInstance formInstanceId(String questionnaireId);
    
    /**
     * Sets the form filling events to apply to the instance.
     * Actions represent user input events like field updates (firstName = "Sam", lastName = "Vimes"),
     * form completion, navigation, and other form interactions.
     * 
     * @param actions the command-style form filling events to merge
     * @return this builder for method chaining
     */
    MergeFormInstance props(Actions actions);
    
    /**
     * Forces the form instance to completion state regardless of validation errors or incomplete fields.
     * When enabled, bypasses normal form validation and completion rules.
     * Use with caution as this may result in incomplete or invalid form submissions.
     * 
     * @param forceCompletion true to force completion, false for normal validation (default)
     * @return this builder for method chaining
     */
    MergeFormInstance forceCompletion(boolean forceCompletion);
    
    /**
     * Executes the merge operation, applying the actions to the form instance.
     * 
     * @return a {@link Uni} containing the updated form instance with new state
     */
    Uni<FormInstance> build();
  }
  
  /**
   * Query builder for retrieving existing form instances from the database.
   * Form instances exist in persistent state regardless of active/inactive status.
   */
  interface FormInstanceQuery {
    
    /**
     * Enables merging of form definition data (valuesets and FormItems) into the questionnaire.
     * When enabled, enriches the questionnaire with valueset data from the form definition,
     * maps answers with their corresponding FormItem metadata, and creates lookup valuesets
     * for answer metadata. This provides access to form field definitions and enhanced
     * valueset entries but increases response size and processing time.
     * 
     * @param includeForm true to merge form data, false for questionnaire only (default)
     * @return this query builder for method chaining
     */
    FormInstanceQuery includeForm(boolean includeForm);
    
    /**
     * Retrieves a specific form instance by its questionnaire ID.
     * Returns the current state of the form instance as stored in the database.
     * When form data is included, the response contains enriched valueset and metadata.
     * 
     * @param questionnaireId the unique identifier of the questionnaire instance
     * @return a {@link Uni} containing the form instance if found  otherwise blows up
     */
    Uni<FormInstance> getOne(String questionnaireId);
    
    /**
     * Sync version of the same named method.
     * 
     * @param questionnaireId the unique identifier of the questionnaire instance
     * @return a {@link Uni} containing the form instance if found otherwise blows up
     */
    FormInstance getOneSync(String questionnaireId);
    
    /**
     * Retrieves a specific form instance by its questionnaire ID.
     * Returns the current state of the form instance as stored in the database.
     * When form data is included, the response contains enriched valueset and metadata.
     * 
     * @param questionnaireId the unique identifier of the questionnaire instance
     * @return a {@link Uni} containing the form instance if found
     */
    Uni<Optional<FormInstance>> findOne(String questionnaireId);
    
    /**
     * Sync version of the same named method.
     * 
     * @param questionnaireId the unique identifier of the questionnaire instance
     * @return a {@link Uni} containing the form instance if found
     */
    Optional<FormInstance> findOneSync(String questionnaireId);
  }
  
  /**
   * Builder for creating new form instances (questionnaires) from form templates.
   * Supports pre-population with context variables and initial answers for form initialization.
   */
  interface CreateFormInstance {
    
    /**
     * Specifies the form template to instantiate.
     * 
     * @param formId the technical identifier of the form template to use
     * @return this builder for method chaining
     */
    CreateFormInstance formId(String formId);
    
    /**
     * Sets the language/locale for form localization.
     * 
     * @param language the language code for form localization (e.g., "en", "fi")
     * @return this builder for method chaining
     */
    CreateFormInstance language(String language);
    
    /**
     * Sets invisible/hidden context variables for the form instance.
     * Context variables are predefined fields that users cannot change directly,
     * such as SSN, name data from login credentials, or system-calculated values.
     * These may be displayed on the form but are not user-editable and can be used
     * for field pre-calculation and default value assignment.
     * 
     * @param ctx map of context variable names to their values
     * @return this builder for method chaining
     */
    CreateFormInstance context(Map<String, Serializable> ctx);
    
    /**
     * Sets initial user answers to pre-populate form fields.
     * These represent actual user responses that can be modified during form completion.
     * 
     * @param answers map of field names to their initial answer values
     * @return this builder for method chaining
     */
    CreateFormInstance answers(Map<String, Serializable> answers);
    
    /**
     * Creates the form instance with the specified configuration.
     * 
     * @return a {@link Uni} containing the questionnaire's unique ID and initial version.
     *         The version changes after each modification to track form state evolution.
     */
    Uni<IdAndRevision> build();
  }
  
  /**
   * Low-level HTTP proxy query interface for retrieving form instances.
   * Provides direct access to raw HTTP responses for performance-critical scenarios.
   * Use this instead of FormInstanceQuery when you need direct HTTP passthrough
   * without domain object marshalling overhead.
   */
  interface FormFillQuery {
    
    /**
     * Retrieves a form instance as raw HTTP response.
     * Returns the underlying ResponseEntity without domain object conversion
     * for direct proxy scenarios and performance optimization.
     * 
     * @param formInstanceId the unique identifier of the questionnaire instance
     * @return a {@link Uni} containing the raw HTTP response wrapper
     */
    Uni<RawResponse> getOne(String formInstanceId);
  }
  /**
   * Query for the flat data document of a COMPLETED form instance, as produced by
   * dialob's printout writer (GET /questionnaires/{id}/session-state). The
   * document is intended as direct input for printout/PDF template pipelines
   * (e.g. Tagomi/Typst).
   */
  interface FormInstanceFlatDataQuery {

    /**
     * Specifies the target form instance.
     *
     * @param formInstanceId the unique identifier of the form instance
     * @return this query builder for method chaining
     */
    FormInstanceFlatDataQuery instanceId(String formInstanceId);

    /**
     * Sets the language override for labels in the flat data document.
     * When not set, dialob falls back to the form instance's own language.
     *
     * @param locale the language code (e.g. "en", "fi"), nullable
     * @return this query builder for method chaining
     */
    FormInstanceFlatDataQuery locale(@Nullable String locale);

    /**
     * Sets the IANA timezone used to render the metadata audit timestamps
     * (created/lastAnswer/lastSaved/completed). Dialob defaults to Europe/Helsinki.
     *
     * @param timeZone the IANA timezone (e.g. "Europe/Helsinki"), nullable
     * @return this query builder for method chaining
     */
    FormInstanceFlatDataQuery timeZone(@Nullable String timeZone);

    /**
     * Retrieves the flat data of the form instance. Never fails - dialob's outcomes
     * are mapped to {@link FormInstanceFlatData.Status} instead.
     *
     * @return a {@link Uni} containing the structured query result
     */
    Uni<FormInstanceFlatData> getOne();

    /**
     * Sync version of the same named method.
     *
     * @return the structured query result
     */
    FormInstanceFlatData getOneSync();

    /**
     * Shortcut for {@code instanceId(formInstanceId).locale(locale).getOne()}.
     *
     * @param formInstanceId the unique identifier of the form instance
     * @param locale the language code override, nullable
     * @return a {@link Uni} containing the structured query result
     */
    default Uni<FormInstanceFlatData> getOne(String formInstanceId, @Nullable String locale) {
      return instanceId(formInstanceId).locale(locale).getOne();
    }

    /**
     * Sync version of the same named method.
     *
     * @param formInstanceId the unique identifier of the form instance
     * @param locale the language code override, nullable
     * @return the structured query result
     */
    default FormInstanceFlatData getOneSync(String formInstanceId, @Nullable String locale) {
      return instanceId(formInstanceId).locale(locale).getOneSync();
    }
  }

  /**
   * Result of a {@link FormInstanceFlatDataQuery}. The body document is present
   * only when the form instance is COMPLETED.
   *
   * <p>Body structure:
   * <pre>{@code
   * {
   *   "id": "…",                       // questionnaire id
   *   "metadata":     { created, creator, formId, formRev, label, language,
   *                     lastAnswer, owner, status, tenantId },   // all String; timestamps zoned per ?tz
   *   "formMetadata": { created, creator, label, lastSaved, savedBy, tenantId },
   *   "contextValues": { "<key>": <value>, … },   // raw context values map
   *   "form":  { "pages": ["page1", …] },          // ordered page ids
   *   "pages": {
   *     "byId":    { "<pageId>": { type, label, hiddenPrint, groupIds:[…] } },
   *     "pageIds": ["page1", …]
   *   },
   *   "groups": {
   *     "byId":  { "<groupId>": { type, label, hiddenPrint, itemIds:[…] } },
   *     "allIds": ["page1", …]                     // top-level group order
   *   },
   *   "items": {
   *     "byId":  { "<itemId>": Item | Note },
   *     "allIds": [ … ]                            // visible non-group leaf ids
   *   }
   * }
   * }</pre>
   */
  @Value.Immutable
  interface FormInstanceFlatData {
    /**
     * @return the form instance (questionnaire) id the query was made with
     */
    String getId();
    Status getStatus();
    Optional<JsonObject> getBody();
    Optional<String> getMessage();

    /**
     * @return the body document encoded as a JSON string, or "{}" when absent
     */
    default String getBodyAsString() {
      return getBody().map(JsonObject::encode).orElse("{}");
    }

    enum Status {
      COMPLETED,
      NOT_COMPLETED,
      NOT_FOUND,
      ERROR
    }
  }
  /**
   * Low-level HTTP proxy builder for form filling operations.
   * Provides direct HTTP passthrough with raw string payloads for performance-critical scenarios.
   * Supports fire-and-forget post-processing callbacks that execute independently
   * after the main HTTP response is sent to the client.
   */
  interface FormFillBuilder {
    
    /**
     * Specifies the target form instance for form filling operations.
     * 
     * @param formInstanceId the unique identifier of the questionnaire instance
     * @return this builder for method chaining
     */
    FormFillBuilder formInstanceId(String formInstanceId);
    
    /**
     * Sets the raw form action payload as string body.
     * Unlike MergeFormInstance.props(), this accepts raw JSON/HTTP payloads
     * without object marshalling for performance optimization.
     * 
     * @param body the raw string payload containing form actions (nullable)
     * @return this builder for method chaining
     */
    FormFillBuilder actions(@Nullable String body);
    
    /**
     * Sets a fire-and-forget callback for post-processing after HTTP response completion.
     * The callback executes on a separate thread after the main request/response cycle
     * has completed and the HTTP connection to the client has been closed.
     * This enables independent post-processing without blocking the UI response.
     * 
     * @param callback consumer that receives a Uni for async post-processing operations.
     * @return this builder for method chaining
     */
    FormFillBuilder onCompletion(Function<String, Uni<?>> callback);
    
    /**
     * Executes the form filling operation and returns raw HTTP response.
     * Returns the underlying ResponseEntity for direct HTTP proxy scenarios.
     * If an onCompletion callback is configured, it will execute independently
     * after this response is sent to the client.
     * 
     * @return a {@link Uni} containing the raw HTTP response wrapper
     */
    Uni<RawResponse> build();
  }

  /**
   * Read-only form state reconstruction engine for displaying completed forms.
   * Creates an in-memory copy of a completed questionnaire and runs it through
   * a Dialob session engine to reconstruct form state for display purposes.
   * Supports navigation to different pages/steps without affecting original data.
   * 
   * <p>Required because Dialob's API requires session reconstruction to properly
   * display completed forms with correct page states and navigation context.
   */
  interface FormFillReview {
    
    /**
     * Specifies the completed form instance to reconstruct for review.
     * The questionnaire will be queried and copied for in-memory simulation.
     * 
     * @param formInstanceId the unique identifier of the completed questionnaire instance
     * @return this builder for method chaining
     */
    FormFillReview formInstanceId(String formInstanceId);
    
    /**
     * Applies navigation actions to the in-memory form copy.
     * Typically used to navigate to specific pages or steps in multi-page forms.
     * All actions are processed without side effects on the original questionnaire.
     * Only meaningful for forms with multiple pages - single-page forms don't need navigation.
     * 
     * @param action the navigation actions to apply (page changes, step navigation, etc.)
     * @return this builder for method chaining
     */
    FormFillReview navigateTo(Actions action);
    
    /**
     * Executes the form state reconstruction and returns the complete rebuilt state.
     * Creates in-memory Dialob session, applies navigation actions, and rebuilds
     * the complete form state as it would appear at the specified navigation point.
     * 
     * @return a {@link Uni} containing Actions representing the complete rebuilt form state
     */
    Uni<Actions> build();
  }
  
  
  interface AnswerAndFormItem {
    Answer getAnswer();
    @Nullable FormItem getFormItem(); //if not available
    Optional<String> getValueSetLabel();
    Optional<FormValueSetEntry> getValueSetEntry();
  }
  

  
  interface FormAndTag {
    String getFormLabel();
    String getFormName();
    String getTagFormId();
    String getTagName();
  }
  
  
  
}
