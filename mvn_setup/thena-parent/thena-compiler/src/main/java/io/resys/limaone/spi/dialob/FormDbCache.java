package io.resys.limaone.spi.dialob;

import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;

/**
 * Cache interface for FormDb operations.
 * Provides caching layer for forms and form tags to reduce HTTP calls.
 */
public interface FormDbCache {
  
  /**
   * Gets a cached form by its technical ID.
   * 
   * @param formId the technical form identifier
   * @return cached form if present, empty otherwise
   */
  Optional<Form> getForm(String formId);
  
  /**
   * Caches a form by its technical ID.
   * 
   * @param formId the technical form identifier
   * @param form the form to cache
   */
  void putForm(String formId, Form form);
  
  /**
   * Gets a cached form tag by form ID and tag name.
   * 
   * @param formId the technical form identifier
   * @param tagName the tag/version name
   * @return cached form tag if present, empty otherwise
   */
  Optional<FormTag> getFormTag(String formId, String tagName);
  
  /**
   * Caches a form tag by form ID and tag name.
   * 
   * @param formId the technical form identifier
   * @param tagName the tag/version name
   * @param formTag the form tag to cache
   */
  void putFormTag(String formId, String tagName, FormTag formTag);
  
  /**
   * Evicts all cached data for a specific form.
   * Use when form is updated or deleted.
   * 
   * @param formId the technical form identifier
   */
  void evictForm(String formId);
  
  /**
   * Evicts all cached data.
   */
  void evictAll();
}