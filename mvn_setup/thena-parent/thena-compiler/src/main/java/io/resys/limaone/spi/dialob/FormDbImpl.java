package io.resys.limaone.spi.dialob;

import java.util.Objects;

import org.immutables.value.Value;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.limaone.spi.dialob.builders.CreateFormImpl;
import io.resys.limaone.spi.dialob.builders.CreateFormInstanceImpl;
import io.resys.limaone.spi.dialob.builders.CreateFormTagImpl;
import io.resys.limaone.spi.dialob.builders.FormInstanceQueryImpl;
import io.resys.limaone.spi.dialob.builders.FormMetaQueryImpl;
import io.resys.limaone.spi.dialob.builders.FormQueryImpl;
import io.resys.limaone.spi.dialob.builders.FormTagQueryImpl;
import io.resys.limaone.spi.dialob.builders.MergeFormImpl;
import io.resys.limaone.spi.dialob.builders.MergeFormInstanceImpl;
import io.resys.limaone.spi.dialob.cache.FormDbCache;
import io.resys.limaone.spi.dialob.cache.FormDbCacheImpl;
import io.resys.limaone.spi.http.HttpClient;
import io.resys.limaone.spi.http.HttpClientImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormDbImpl implements FormDb {

  private final FormDbProps formDbProps;
  private final String tenantName;
  
  @Override
  public String getTenantName() {
    return tenantName;
  }
  @Override
  public FormTenant withTenant() {
    return withTenant(tenantName);
  }
  @Override
  public FormTenant withTenant(String tenantIdOrName) {
    return new FormTenant() {
      @Override public String getTenantId() { return tenantIdOrName; }
      @Override public CreateForm createForm() { return new CreateFormImpl(formDbProps); }
      @Override public FormQuery formQuery() { return new FormQueryImpl(formDbProps); }
      @Override public FormTagQuery formTagQuery() { return new FormTagQueryImpl(formDbProps); }
      @Override public FormMetaQuery formMetaQuery() { return new FormMetaQueryImpl(formDbProps); }
      @Override public CreateFormTag createFormTag() { return new CreateFormTagImpl(formDbProps); }
      @Override public MergeForm mergeForm() { return new MergeFormImpl(formDbProps); }
      @Override public MergeFormInstance mergeFormInstance() { return new MergeFormInstanceImpl(formDbProps); }
      @Override public FormInstanceQuery formInstanceQuery() { return new FormInstanceQueryImpl(formDbProps); }
      @Override public CreateFormInstance createFormInstance() { return new CreateFormInstanceImpl(formDbProps); }
    };
  }

  public static class FormDbBuilder {
    private ObjectMapper objectMapper;
    private RestTemplate questionnaireHttp;
    private RestTemplate formHttp;
    private FormDbCache cache;
    
    public FormDbBuilder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public FormDbBuilder formHttp(RestTemplate formHttp) {
      this.formHttp = formHttp;
      return this;
    }
    public FormDbBuilder questionnaireHttp(RestTemplate questionnaireHttp) {
      this.questionnaireHttp = questionnaireHttp;
      return this;
    }
    public FormDbBuilder cache(FormDbCache cache) {
      this.cache = cache;
      return this;
    }
    
    public FormDbImpl build() {
      Objects.requireNonNull(objectMapper, () -> "objectMapper must be defined");
      Objects.requireNonNull(formHttp, () -> "formHttp must be defined");
      Objects.requireNonNull(questionnaireHttp, () -> "questionnaireHttp must be defined");
      
      final var formHttp = new HttpClientImpl(this.formHttp, objectMapper);
      final var questionnaireHttp = new HttpClientImpl(this.questionnaireHttp, objectMapper);
      final var formDbCache = cache != null ? cache : new FormDbCacheImpl();
      
      return new FormDbImpl(
        ImmutableFormDbProps.builder()
          .formHttp(formHttp)
          .questionnaireHttp(questionnaireHttp)
          .cache(formDbCache)
          .build(), 
        "default"
      );
    }
  }
  
  public static FormDbBuilder builder() {
    return new FormDbBuilder();
  }
  
  @Value.Immutable
  public interface FormDbProps {
    HttpClient getFormHttp();
    HttpClient getQuestionnaireHttp();
    FormDbCache getCache();
  }

  public FormDbProps getFormDbProps() {
    return formDbProps;
  }
}
