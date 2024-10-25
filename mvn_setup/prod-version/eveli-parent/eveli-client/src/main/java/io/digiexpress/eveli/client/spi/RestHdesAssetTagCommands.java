package io.digiexpress.eveli.client.spi;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

// should be direct integration
//@Slf4j
/* @AllArgsConstructor
public class RestHdesAssetTagCommands implements JsonNodeTagCommands<AnyAssetTag> {
  private final RestTemplate client;

  private final String baseUrl;
  

  @Override
  public AnyAssetTag createTag(AssetTagInit init) {
    Mutable<AnyAssetTag> result = new MutableObject<>(null);

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
  public List<AnyAssetTag> findAll() {
    List<AnyAssetTag> result = new ArrayList<>();

    JsonNode state = getTagNodes();
    processTags(state, (tagAst,id)-> {
      result.add(convertAstTag(tagAst, id));
    });
    return result;
  }

  @Override
  public Optional<AnyAssetTag> getByName(String name) {
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
  
  private AnyAssetTag convertAstTag(JsonNode node, String id) {
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
  
}*/
