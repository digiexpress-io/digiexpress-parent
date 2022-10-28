package io.digiexpress.client.spi.builders.visitors;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableProcessDocument;
import io.digiexpress.client.api.ImmutableProcessRevisionDocument;
import io.digiexpress.client.api.ImmutableProcessRevisionValue;
import io.digiexpress.client.api.ImmutableRefIdValue;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer.CreateRevision;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.DocumentType;
import io.digiexpress.client.api.ServiceDocument.RefIdValue;
import io.digiexpress.client.api.ServiceStore.CreateStoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreCommand;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class CreateRevisionVisitor {
  private final ServiceClient client;
  private final CreateRevision init;
  private final LocalDateTime now = LocalDateTime.now();
  
  @Builder @Data
  public static class Result {
    private final ServiceDocument.ProcessDocument process;
    private final ServiceDocument.ProcessRevisionDocument revision;
    private final List<StoreCommand> batch;
  }
  
  public CreateRevisionVisitor.Result visit() {
    final var refs = init.getValues().stream()
        .map(e -> (RefIdValue) ImmutableRefIdValue.builder().from(e).build())
        .collect(Collectors.toList());
    
    final var process = ImmutableProcessDocument.builder()
      .id(nextId(DocumentType.PROCESS_DEF)).version(nextId(DocumentType.PROCESS_DEF)).created(now).updated(now)
      .devMode(true)
      .processName(init.getName())
      .addAllValues(refs)
      .build();

    final var head = ImmutableProcessRevisionValue.builder()
      .id(nextId(DocumentType.PROCESS_REV)).created(now).updated(now)
      .revisionName("init")
      .processDocumentId(process.getId())
      .build();
      
    final var rev = ImmutableProcessRevisionDocument.builder()
      .id(nextId(DocumentType.PROCESS_REV)).version(nextId(DocumentType.PROCESS_REV)).created(now).updated(now)
      .name(init.getName())
      .head(head.getId())
      .type(DocumentType.PROCESS_REV)
      .addValues(head)
      .build();
    
    return Result.builder()
        .process(process)
        .revision(rev)
        .batch(Arrays.asList(
            toStoreCommand(rev),
            toStoreCommand(process)
        ))
        .build();
  }
  
  protected CreateStoreEntity toStoreCommand(ServiceDocument doc) {
    return ImmutableCreateStoreEntity.builder()
        .bodyType(doc.getType())
        .id(doc.getId())
        .version(doc.getVersion())
        .body(client.getConfig().getMapper().toBody(doc))
        .build();
  } 
  
  protected String nextId(DocumentType type) {
    return client.getConfig().getStore().getGid().getNextId(type);
  }
}
