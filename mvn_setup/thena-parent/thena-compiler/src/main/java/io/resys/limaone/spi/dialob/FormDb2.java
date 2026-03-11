package io.resys.limaone.spi.dialob;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.dialob.api.form.ImmutableFormTag;
import io.dialob.api.form.FormTag.Type;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.dialob.api.DialobClientProxy;
import io.digiexpress.eveli.dialob.api.DialobClient.Dialob;
import io.digiexpress.eveli.dialob.api.DialobClient.FormListItem;
import io.digiexpress.eveli.dialob.api.DialobClient.ProxyAnswer;
import io.digiexpress.eveli.dialob.spi.DialobAssert;
import io.digiexpress.eveli.dialob.spi.DialobClientImpl;
import io.digiexpress.eveli.dialob.spi.DialobProxyImpl;
import io.digiexpress.eveli.dialob.spi.DialobService;
import io.digiexpress.eveli.dialob.spi.DialobSessionBuilderImpl;
import io.digiexpress.eveli.dialob.spi.QuestionnaireWrapperImpl;
import io.digiexpress.eveli.dialob.spi.DialobAssert.DialobException;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public interface FormDb2 {
  
  
  private final DialobService dialobService;

  

@Data
@RequiredArgsConstructor
public class DialobService {
  private final RestTemplate api;
  private final RestTemplate sessions;
}


@RequiredArgsConstructor
public class DialobProxyImpl implements DialobClientProxy {
  private final DialobService dialobService;
  final var reqHeaders = new HttpHeaders();
  headers.entrySet().stream()
    .filter(entry ->
      entry.getKey().equals(HttpHeaderNames.CONTENT_TYPE.toString()) ||
      entry.getKey().equals(HttpHeaderNames.ACCEPT.toString())
    )
    .forEach((entry) -> reqHeaders.put(entry.getKey(), Collections.singletonList(entry.getValue())));
  return dialobService.getApi().exchange("/forms" + path, method, new HttpEntity<>(body, reqHeaders), String.class);
}
  

  @Override
  public List<FormListItem> findAllForms() {
    try {
      final String body = createProxyClient().formRequest("", "", HttpMethod.GET, null, Collections.emptyMap()).getBody();
      return Arrays.asList(objectMapper.readerForArrayOf(FormListItem.class).readValue(body));
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  

  @Override
  public List<FormTag> findAllFormTags() {
    try {
      final String body = createProxyClient().tagsRequest("", "", HttpMethod.GET, null, Collections.emptyMap()).getBody();
      return Arrays.asList(objectMapper.readerForArrayOf(FormTag.class).readValue(body));
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  

  @Override
  public Form createForm(Form form) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(form);
      final var resp = createProxyClient().formRequest("", null, HttpMethod.POST, body, headers);
      return objectMapper.readValue(resp.getBody(), Form.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  
  @Override
  public Form updateForm(Form form) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(form);
      final var resp = createProxyClient().formRequest("/" + form.getId(), null, HttpMethod.PUT, body, headers);
      return objectMapper.readValue(resp.getBody(), Form.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  
  @Override
  public FormTag createTag(String formId, String tagName) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(ImmutableFormTag.builder()
          .name(tagName)
          .formName(formId)
          .type(Type.NORMAL)
          .build());
      final var postTagResp = createProxyClient().formRequest("/" + formId + "/tags", null, HttpMethod.POST, body, headers);
      DialobAssert.isTrue(postTagResp.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + postTagResp.getStatusCode() + " but expecting 2xx!");
      
      
      final var getTagResp = createProxyClient().formRequest("/" + formId + "/tags/" + tagName, null, HttpMethod.GET, body, headers);
      DialobAssert.isTrue(getTagResp.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getTagResp.getStatusCode() + " but expecting 2xx!");
      
      return objectMapper.readValue(getTagResp.getBody(), FormTag.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  @Override
  public Form getFormById(String formId) {
    return dialobService.getApi().getForObject("/forms/" + formId, Form.class);
  }
  @Override
  public Form getFormByNameAndTag(String formName, String formTag) {
    final var uri = "/forms/" + formName + "/tags/" + formTag;
    final FormTag tag = dialobService.getApi().getForObject(uri, FormTag.class);
    String taggedFormId = tag.getFormId();
    return dialobService.getApi().getForObject("/forms/" + taggedFormId, Form.class);
  }
  @Override
  public FormTag getFormTag(String formName, String formTag) {
    final var uri = "/forms/" + formName + "/tags/" + formTag;
    return dialobService.getApi().getForObject(uri, FormTag.class);
  }
  @Override
  public List<FormTag> getOneFormTags(String formName) {
    final var uri = "/forms/" + formName + "/tags";
    final var tags = dialobService.getApi().getForObject(uri, FormTag[].class);
    return Arrays.asList(tags);
  }
  @Override
  public Optional<Form> findOneFormById(String formId) {
    try {
      return Optional.ofNullable(dialobService.getApi().getForObject("/forms/" + formId, Form.class));
    } catch(org.springframework.web.client.HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
