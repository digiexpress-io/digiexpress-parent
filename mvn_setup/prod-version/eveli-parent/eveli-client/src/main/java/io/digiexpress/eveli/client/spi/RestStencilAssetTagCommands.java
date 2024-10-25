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

/*should be direct integration
@Slf4j
@AllArgsConstructor
public class RestStencilAssetTagCommands implements JsonNodeTagCommands<AnyAssetTag> {
  private final RestTemplate client;

  private final String baseUrl;
  

  @Override
  public AnyAssetTag createTag(AssetTagInit init) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    final String url = UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment("releases").toUriString();
    log.debug("Creating Stencil tag at URL {}", url);
    ResponseEntity<JsonNode> response = client.exchange(url, 
        HttpMethod.POST, 
        new HttpEntity<>(getTagInput(init), headers),
        JsonNode.class);
    Assert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Tag creation status was: " + response.getStatusCode() + " but expecting 200!");
    JsonNode result = response.getBody();
    AnyAssetTag retVal = convertAstTag(result.path("body"), result.path("id").textValue());
    
    return retVal;
  }

  @Override
  public List<AnyAssetTag> findAll() {
    List<AnyAssetTag> result = new ArrayList<>();
    JsonNode dataModel = readDataModels();
    processReleases(dataModel, (release, id)-> {
      AnyAssetTag tag = convertAstTag(release, id);
      result.add(tag);
    });
    return result;
  }
  
  @Override
  public Optional<AnyAssetTag> getByName(String name) {
    Mutable<AnyAssetTag> result = new MutableObject<>(null);

    JsonNode dataModel = readDataModels();
    processReleases(dataModel, (release, id)-> {
      if (name.equals(release.path("name").asText())) {
        AnyAssetTag tag = convertAstTag(release, id);
        result.setValue(tag);
      }
    });
    
    return Optional.ofNullable(result.getValue());
  }

  private JsonNode readDataModels() {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    final String url = UriComponentsBuilder.fromHttpUrl(baseUrl).toUriString();
    log.debug("Getting Stencil data models at URL {}", url);
    ResponseEntity<JsonNode> response = client.exchange(url, 
        HttpMethod.GET, 
        new HttpEntity<>(headers),
        JsonNode.class);
    Assert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Find all tag status was: " + response.getStatusCode() + " but expecting 200!");
    
    JsonNode body = response.getBody();
    return body;
  }

  private JsonNode readRelease(String releaseId) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    final URI url = UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment("releases").pathSegment("{releaseId}").buildAndExpand(releaseId).toUri();
    log.debug("Getting Stencil release at URL {}", url);
    ResponseEntity<JsonNode> response = client.exchange(url, 
        HttpMethod.GET, 
        new HttpEntity<>(headers),
        JsonNode.class);
    Assert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Find all tag status was: " + response.getStatusCode() + " but expecting 200!");
    
    JsonNode body = response.getBody();
    return body;
  }
  
  private void processReleases(JsonNode dataModel, BiConsumer<JsonNode, String> releaseProcessor) {
    JsonNode releases = dataModel.path("releases");
    Iterator<Entry<String, JsonNode>> releaseVersions = releases.fields();
    while (releaseVersions.hasNext()) {
      Entry<String, JsonNode> jsonField = releaseVersions.next();
      JsonNode releaseBody = jsonField.getValue().path("body");
      releaseProcessor.accept(releaseBody, jsonField.getKey());
    }
  }

  private Map<String, Object> getTagInput(AssetTagInit ati) {
    Map<String, Object> result = new HashMap<>();
    result.put("name", ati.getName());
    result.put("note", ati.getDescription());
    return result;
  }
  
  private AnyAssetTag convertAstTag(JsonNode node, String id) {
    return ImmutableAssetTag.builder()
        .id(id)
        .name(node.path("name").asText())
        .description(node.path("note").asText())
        .created(LocalDateTime.parse(node.path("created").asText()))
        .build();
  }

  @Override
  public JsonNode getTagAssets(String tag) {
    log.debug("Requested content tag {} from assets, reading assets", tag);
    Optional<AnyAssetTag> assetTag = getByName(tag);
    if (assetTag.isPresent()) {
      log.debug("Found asset tag with id {}", assetTag.get().getId());
      return readRelease(assetTag.get().getId());
    }
    else {
      log.warn("Asset tag with name {} not found", tag);
      return null;
    }
  }
}
*/
