package io.digiexpress.eveli.client.web.resources.gamut;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.beans.LocalizedSiteBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/gamut/sites")
@RequiredArgsConstructor
public class GamutSiteController {
  
  
  private final StencilClient client;

  @GetMapping(path = "/")
  public Uni<LocalizedSite> getOneSiteByLocale(@RequestParam(name = "locale") String locale) {
   
    
    return client.getStore().query().head()
    .onItem().transform(state -> client.markdown().json(state, true).build())
    .onItem().transform(markdowns -> client.sites()
        .imagePath("images")
        .created(System.currentTimeMillis())
        .source(markdowns)
        .build())
    .onItem().transform(sites -> sites.getSites().get(locale))
    .onItem().transform(data -> {
      if(data == null) {
        return LocalizedSiteBean.builder().id("not-found")
            .images("images")
            .locale(locale)
            .build();
      }
      return LocalizedSiteBean.builder().from(data).id(data.getId()).build();
    });
    
  }
}
