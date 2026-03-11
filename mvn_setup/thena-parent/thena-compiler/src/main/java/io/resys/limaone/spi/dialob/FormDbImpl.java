package io.resys.limaone.spi.dialob;

import java.util.Objects;

import org.immutables.value.Value;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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
      @Override public CreateForm createForm() { return null; }
      @Override public FormQuery formQuery() { return null; }
      @Override public FormTagQuery formTagQuery() { return null; }
      @Override public FormMetaQuery formMetaQuery() { return null; }
      @Override public CreateFormTag createFormTag() { return null; }
      @Override public MergeForm mergeForm() { return null; }
    };
  }

  public static class FormDbBuilder {
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    
    public FormDbBuilder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public FormDbBuilder restTemplate(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
      return this;
    }
    
    public FormDbImpl build() {
      Objects.requireNonNull(objectMapper, () -> "objectMapper must be defined");
      Objects.requireNonNull(restTemplate, () -> "restTemplate must be defined");
      final var http = new HttpClientImpl(restTemplate, objectMapper);
      return new FormDbImpl(ImmutableFormDbProps.builder().client(http).build(), "default");
    }
  }
  
  public static FormDbBuilder builder() {
    return new FormDbBuilder();
  }
  
  @Value.Immutable
  public interface FormDbProps {
    HttpClient getClient();
  }
}
