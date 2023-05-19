package io.digiexpress.client.spi.query;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.spi.support.MainBranch;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetStencilTag {
  private final ClientConfig config;
  private final String tagName;
  private final String imagePath = "/images";
  
  public Uni<MigrationBuilder.Sites> build() {
    final var isMain = MainBranch.isMain(tagName);
    
    return config.getStencil().getStore().query().head()
    .onItem().transformToUni((SiteState e) -> {
      if(isMain) {        
        final var md = config.getStencil().markdown().json(e, false).build();
        final var site = config.getStencil().sites().created(System.currentTimeMillis()).imagePath(imagePath).source(md).build();
        return Uni.createFrom().item(site);
      }
      
      final var foundTag = e.getReleases().values().stream()
          .filter(r -> r.getId().equals(tagName) || r.getBody().getName().equals(tagName))
          .findFirst();
      ServiceAssert.notNull(foundTag, () -> "Can't find stencil tag with id or name: '" + tagName + "'");
      
      
      return config.getStencil().getStore().query().release(foundTag.get().getId()).onItem()
      .transform(release -> {
        final var md = config.getStencil().markdown().json(release, false).build();
        return config.getStencil().sites().created(System.currentTimeMillis()).source(md).build();
      });
    });
  }
}
