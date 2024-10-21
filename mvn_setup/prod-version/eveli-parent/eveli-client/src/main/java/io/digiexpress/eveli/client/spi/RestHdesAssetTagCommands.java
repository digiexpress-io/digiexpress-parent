package io.digiexpress.eveli.client.spi;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import io.digiexpress.eveli.client.api.AssetTagCommands.AssetTag;
import io.digiexpress.eveli.client.api.ImmutableAssetTag;
import io.digiexpress.eveli.client.api.JsonNodeTagCommands;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RestHdesAssetTagCommands implements JsonNodeTagCommands<AssetTag> {
  private final RestTemplate client;

  private final String baseUrl;
  

  @Override
  public AssetTag createTag(AssetTagInit init) {
    Mutable<AssetTag> result = new MutableObject<>(null);

    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    final String url = UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment("resources").toUriString();
    log.debug("Creating wrench tag at URL {}", url);
    ResponseEntity<JsonNode> response = client.exchange(url, 
        HttpMethod.POST, 
        new HttpEntity<>(getTagInput(init), headers),
        JsonNode.class);
    Assert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Tag creation status was: " + response.getStatusCode() + " but expecting 200!");
    JsonNode state = response.getBody();
    
    processTags(state, (tagAst, id)-> {
      String name = tagAst.path("name").asText();
      if (init.getName().equals(name)) {
        result.setValue(convertAstTag(tagAst, id));
      }
    });
    return result.getValue();
  }


  @Override
  public List<AssetTag> findAll() {
    List<AssetTag> result = new ArrayList<>();

    JsonNode state = getTagNodes();
    processTags(state, (tagAst,id)-> {
      result.add(convertAstTag(tagAst, id));
    });
    return result;
  }

  @Override
  public Optional<AssetTag> getByName(String name) {
    JsonNode state = getTag(name);
    JsonNode tagAst = state.path("ast");
    String id = state.path("id").asText();
    var result = convertAstTag(tagAst, id);

    return Optional.ofNullable(result);
  }


  private JsonNode getTagNodes() {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    final String url = UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment("dataModels").toUriString();
    log.debug("Getting all wrench tags at URL {}", url);
    ResponseEntity<JsonNode> response = client.exchange(url, 
        HttpMethod.GET, 
        new HttpEntity<>(headers),
        JsonNode.class);
    Assert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Find all tag status was: " + response.getStatusCode() + " but expecting 200!");
    return response.getBody();
  }

  private JsonNode getTag(String tag) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    final URI url = UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment("resources").pathSegment("{tag}").buildAndExpand(tag).toUri();
    log.debug("Getting wrench tag at URL {}", url);
    ResponseEntity<JsonNode> response = client.exchange(url, 
        HttpMethod.GET, 
        new HttpEntity<>(headers),
        JsonNode.class);
    Assert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Find all tag status was: " + response.getStatusCode() + " but expecting 200!");
    return response.getBody();
  }

  protected CreateEntity getTagInput(AssetTagInit ati) {
    return ImmutableCreateEntity.builder().name(ati.getName()).desc(ati.getDescription()).type(AstBodyType.TAG).build();
  }
  
  private void processTags(JsonNode dataModel, BiConsumer<JsonNode,String> tagProcessor) {
    JsonNode tags = dataModel.path("tags");
    Iterator<Entry<String, JsonNode>> releaseVersions = tags.fields();
    while (releaseVersions.hasNext()) {
      Entry<String, JsonNode> jsonField = releaseVersions.next();
      JsonNode tagAst = jsonField.getValue().path("ast");
      tagProcessor.accept(tagAst, jsonField.getKey());
    }
  }
  
  private AssetTag convertAstTag(JsonNode node, String id) {
    return ImmutableAssetTag.builder()
        .id(id)
        .name(node.path("name").asText())
        .description(node.path("description").asText())
        .created(LocalDateTime.parse(node.path("created").asText()))
        .build();
  }


  @Override
  public JsonNode getTagAssets(String tag) {

    JsonNode state = getTag(tag);
    JsonNode tagAst = state.path("ast");
    return tagAst;
  }
}
