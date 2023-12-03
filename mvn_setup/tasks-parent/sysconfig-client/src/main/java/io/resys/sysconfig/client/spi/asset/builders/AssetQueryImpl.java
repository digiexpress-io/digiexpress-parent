package io.resys.sysconfig.client.spi.asset.builders;

import java.util.List;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobStore;
import io.resys.hdes.client.api.HdesStore;
import io.resys.sysconfig.client.api.AssetClient.Asset;
import io.resys.sysconfig.client.api.AssetClient.AssetClientConfig;
import io.resys.sysconfig.client.api.AssetClient.AssetQuery;
import io.resys.sysconfig.client.api.AssetClient.DialobAsset;
import io.resys.sysconfig.client.api.ImmutableDialobAsset;
import io.resys.sysconfig.client.spi.asset.AssetClientConfig.AssetClients;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.sysconfig.client.spi.support.ErrorMsg;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetQueryImpl implements AssetQuery {

  private final Uni<AssetClientConfig> clients;

  @Override
  public Multi<Asset> findAll() {
    // TODO Auto-generated method stub
    return null;
  }

  public void doWithClients(AssetClients clients) {
    
  }
  
  public void doWithStencil(AssetClients clients, StencilComposer.SiteState store) {
    
  }  
  
  public void doWithHdes(AssetClients clients, HdesStore.StoreState store) {
    
  } 

  @Override
  public Uni<Asset> getWrenchAsset(String releaseId) {

    return null;
  }

  @Override
  public Uni<Asset> getStencilAsset(String releaseId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<List<DialobAsset>> getDialobAssets(List<String> formId) {
    return this.clients.onItem().transformToUni(configs -> {
      final DialobClient client = configs.getDialob();
      final DialobStore store = client.getConfig().getStore();
      final Uni<DialobStore.StoreState> state = store.query().get();
      return state.onItem().transform(loaded -> findFormsById(client, loaded, formId));
    });
  }
  public List<DialobAsset> findFormsById(DialobClient client, DialobStore.StoreState store, List<String> formId) {
    final var forms = formId.stream().map(id -> store.getForms().get(id)).filter(e -> e != null).toList();

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
          .formTechnicalName(form.getName())
          .id(form.getId())
          .build();
      
      return asset;
    }).toList();
  }
  
  
  private boolean isReleaseId(String id) {
    final var criteria = id == null ? "": id.toLowerCase().trim();
    if(criteria.isEmpty()) {
      return false;
    }
    if(id.equals("main")) {
      return false;
    } else if(id.equals("latest")) {
      return false;
    } else if(id.equals("dev")) {
      return false;
    }
    return true;
  }
}
