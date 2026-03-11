package io.resys.limaone.spi.dialob;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;

public class FormDbCacheImpl implements FormDbCache {
  
  private final Cache<String, Form> formCache;
  private final Cache<String, FormTag> formTagCache;
  
  public FormDbCacheImpl() {
    this.formCache = Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .build();
    
    this.formTagCache = Caffeine.newBuilder()
      .maximumSize(5000)
      .expireAfterWrite(60, TimeUnit.MINUTES)
      .build();
  }
  
  @Override
  public Optional<Form> getForm(String formId) {
    return Optional.ofNullable(formCache.getIfPresent(formId));
  }
  
  @Override
  public void putForm(String formId, Form form) {
    formCache.put(formId, form);
  }
  
  @Override
  public Optional<FormTag> getFormTag(String formId, String tagName) {
    return Optional.ofNullable(formTagCache.getIfPresent(formId + ":" + tagName));
  }
  
  @Override
  public void putFormTag(String formId, String tagName, FormTag formTag) {
    formTagCache.put(formId + ":" + tagName, formTag);
  }
  
  @Override
  public void evictForm(String formId) {
    formCache.invalidate(formId);
    // Evict all tags for this form
    formTagCache.asMap().keySet().removeIf(key -> key.startsWith(formId + ":"));
  }
  
  @Override
  public void evictAll() {
    formCache.invalidateAll();
    formTagCache.invalidateAll();
  }
}