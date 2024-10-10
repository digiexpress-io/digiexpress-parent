package io.dialob.client.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.ImmutableContextValue;
import io.dialob.api.questionnaire.ImmutableQuestionnaire;
import io.dialob.api.questionnaire.ImmutableQuestionnaireMetadata;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobFill;
import io.dialob.client.api.ImmutableFillEntry;
import io.dialob.client.api.ImmutableStoreExceptionMsg;
import io.dialob.client.spi.exceptions.StoreException;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.spi.Constants;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobInMemoryFill implements DialobFill {
  private static final String CACHE_PREFIX = DialobInMemoryFill.class.getCanonicalName();
  private final CacheManager cacheManager;
  private final DialobClient client;
  private final String cacheName;
  
  
  @Value.Immutable
  interface FillCacheEntry extends Serializable {
    String getId();
    Questionnaire getQuestionnaire();
    Form getForm();
  }
  
  private Cache<String, FillEntry> getCache() {
    return cacheManager.getCache(cacheName, String.class, FillEntry.class);
  }
  
  @Override
  public FillBuilder create() {
    return new FillBuilder() {
      private String formId;
      private String language;
      private final List<ContextValue> contextValues = new ArrayList<>();
      @Override public FillBuilder formId(String formId) { this.formId = formId; return this; }
      @Override public FillBuilder language(String language) { this.language = language; return this; }
      @Override public FillBuilder contextValues(Collection<ContextValue> contextValues) {
        this.contextValues.addAll(contextValues);
        return this; 
      }
      @Override public FillBuilder contextValues(Map<String, Object> contextValues) {
        contextValues.forEach((key, value) -> {
          this.contextValues.add(ImmutableContextValue.builder().id(key).value(value).build());
        });
        return this; 
      }
      @Override public Uni<FillEntry> build() {
        DialobAssert.notEmpty(formId, () -> "formId can't be empty!");
        DialobAssert.notEmpty(language, () -> "language can't be empty!");
        
        final var metadata = ImmutableQuestionnaireMetadata.builder().formId(formId).formRev(Constants.LATEST_REV).language(language).build();
        final var next = ImmutableQuestionnaire.builder().metadata(metadata)
            .addAllContext(contextValues)
            .build();
        
        return save(next).onItem().transform(saved -> {
          final var cache = getCache();
          return cache.get(saved.getId());        
        });
        
      }
    };
  }

  @Override
  public FillQuery query() {
    return new FillQuery() {
      private String formId;
      @Override
      public Uni<Questionnaire> get(String id) {
        final var cache = getCache();
        final var entry = cache.get(id);
        if(entry != null) {
          return Uni.createFrom().item(entry.getQuestionnaire());
        }
        final var result = StreamSupport.stream(cache.spliterator(), true)
          .filter(e -> id.contains(e.getValue().getQuestionnaire().getId()))
          .findFirst();
        
        if(result.isEmpty()) {
          throw new StoreException("FILL_NOT_FOUND", null, 
            ImmutableStoreExceptionMsg.builder()
            .id("FILL_NOT_FOUND")
            .value("Fill not found with id: " + id)
            .addArgs(id)
            .build()); 
        }        
        return Uni.createFrom().item(result.get().getValue().getQuestionnaire());
      }
      @Override
      public FillQuery formId(String formId) {
        this.formId = formId;
        return this;
      }
      @Override
      public Uni<List<Questionnaire>> find() {
        final var cache = getCache();
        final var result = StreamSupport.stream(cache.spliterator(), true)
          .filter(e -> e.getValue().getForm().getId().equals(formId))
          .map(e -> e.getValue().getQuestionnaire())
          .collect(Collectors.toList());
        return Uni.createFrom().item(result);
      }
    };
  }

  @Override
  public Uni<Questionnaire> save(Questionnaire init) {
    DialobAssert.notNull(init, () -> "init can't be null!");
    final var cache = getCache();
    final var saved = init.getId() == null ? null : cache.get(init.getId());
    final Questionnaire next;
    if(saved == null) {
      next = ImmutableQuestionnaire.builder().id(UUID.randomUUID().toString()).from(init).build();
    } else {
      next = init;
    }
    return client.store().query().get()
      .onItem().transform(state -> {
        final var toBeSaved = ImmutableQuestionnaire.builder().from(next).rev(UUID.randomUUID().toString()).build();
        final var formId = toBeSaved.getMetadata().getFormId();
        final var composerState = DialobComposerImpl.composerState(client, state);
        final var form = composerState.getForms().get(formId);
        
        if(form == null) {
          throw new StoreException("FORM_NOT_FOUND", null, 
            ImmutableStoreExceptionMsg.builder()
            .id("FORM_NOT_FOUND")
            .value("form not found with id: " + formId)
            .addArgs(formId)
            .build());
        }
        
        final var entry = ImmutableFillEntry.builder()
          .id(toBeSaved.getId())
          .questionnaire(toBeSaved)
          .form(form.getData())
          .build();

        cache.put(entry.getId(), entry);      
        
        return entry.getQuestionnaire();
      });
  }

  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    
    public DialobInMemoryFill build(String name, DialobClient client) {
      final var cacheName = createName(name);
      final var cacheHeap = 300;
      final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
          .withCache(cacheName,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, FillEntry.class, 
                  ResourcePoolsBuilder.heap(cacheHeap))) 
          .build(); 
      cacheManager.init();
      
      return new DialobInMemoryFill(cacheManager, client, cacheName);
    }
  }

  private static String createName(String name) {
    return CACHE_PREFIX + "-" + name;
  }
}
