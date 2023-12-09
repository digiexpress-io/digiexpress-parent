package io.digiexpress.client.spi.query;

import java.util.Optional;

import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetDialobFormRev {
  private final ClientConfig config;
  private final String formId;
  
  
  public Uni<FormRevisionDocument> build() {
    final var mapper = config.getDialob().getConfig().getMapper();
    return config.getDialob().getConfig().getStore()
    .query().get()
    .onItem().transform(state -> {
      final Optional<FormRevisionDocument> result = state.getRevs().values().stream().map(mapper::toFormRevDoc)
      .filter(rev -> {
        if(rev.getId().equals(formId) || rev.getName().equals(formId)) {
          return true;
        }
        final var containsEntry = rev.getEntries().stream()
            .filter((e) -> 
                e.getFormId().equals(formId) || 
                e.getRevisionName().equals(formId) || 
                formId.equals(e.getId())  
            ).findFirst().isPresent();
        return containsEntry;
      })
      .findFirst();
      
      ServiceAssert.isTrue(result.isPresent(), () -> "Can't find form rev by id or name: '" + formId + "'!");
      return result.get();
    });
  }
}
