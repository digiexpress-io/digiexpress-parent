package io.resys.sysconfig.client.spi.asset.visitors;

import java.time.Instant;
import java.util.List;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobStore;
import io.resys.sysconfig.client.api.AssetClient.DialobAsset;
import io.resys.sysconfig.client.api.ImmutableDialobAsset;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.thena.docdb.support.ErrorMsg;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindDialobFormsVisitor {

  private final DialobClient client;
  private final DialobStore store;
  private final DialobStore.StoreState state;
  
  public List<DialobAsset> visit(List<String> formId) {
    final var forms = formId.stream().map(id -> state.getForms().get(id)).filter(e -> e != null).toList();

    if(forms.size() != formId.size()) {
      final var found = forms.stream().map(e -> e.getId()).toList();
      final var missing = formId.stream().filter(m -> !found.contains(m));
      
      throw new AssetClientException(ErrorMsg.builder()
          .withCode("DIALOB_FORMS_PARTIALLY_FOUND")
          .withProps(JsonObject.of(
              "found", found,
              "missing", missing
              ))
          .withMessage("Some (" + missing.count() + " count) of the queried forms where not found!")
          .toString()); 
    }
    
    
    return forms.stream().map(found -> {
      final var form = client.getConfig().getMapper().readForm(found.getBody());
    
      final DialobAsset asset = ImmutableDialobAsset.builder()
          .formTagName("")
          .created(Instant.now())
          .id(found.getId())
          .name(form.getName())
          .assetBody(found.getBody())
          .version(found.getVersion())
          .build();
      
      return asset;
    }).toList();
  }
}
