package io.digiexpress.eveli.client.web.resources.assets;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.hdes.client.api.HdesClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/any-tags")
@Slf4j
public class AssetsAnyTagController {
  private final StencilClient stencilClient;
  private final HdesClient hdesClient;
  
  
  enum AssetTagType {
    WRENCH, STENCIL
  }  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAnyAssetTag.class)
  @JsonDeserialize(as = ImmutableAnyAssetTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface AnyAssetTag {
    String getId();
    String getName();
    String getDescription();
    LocalDateTime getCreated();
    AssetTagType getType();
    
    // some models don't store user info for tag
    @Nullable String getUser();
  }
  
  
  @GetMapping("/wrench-tags")
  public Uni<List<AnyAssetTag>> findAllWrenchTags() {
    return hdesClient.store().query().get().onItem().transform(state ->
      state.getTags().values().stream()
          .map(release -> hdesClient.ast().commands(release.getBody()).tag())
          .map(release ->  (AnyAssetTag) ImmutableAnyAssetTag.builder()
          .created(release.getCreated())
          .type(AssetTagType.WRENCH)
          .description(release.getDescription())
          .id("no-release-id")
          .name(release.getName())

          .user("not-available")
          .build())
          .toList()
    );
  
  }

  @GetMapping("/stencil-tags")
  public Uni<List<AnyAssetTag>> findAllContentTags() {
    return stencilClient.getStore().query().head().onItem().transform(state -> 
      state.getReleases().values().stream()
          .map(release -> (AnyAssetTag) ImmutableAnyAssetTag.builder()
          .created(release.getBody().getCreated())
          .type(AssetTagType.STENCIL)
          .description(release.getBody().getNote())
          .id(release.getId())
          .name(release.getBody().getName())

          .user("not-available")
          .build())
          .toList()
    );
  }
}
