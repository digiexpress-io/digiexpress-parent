package io.digiexpress.client.spi.composer.visitors;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobStore;
import io.dialob.client.api.DialobStore.StoreCommand;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableFormRevisionDocument;
import io.dialob.client.spi.support.OidUtils;
import io.digiexpress.client.api.ImmutableMigrationState;
import io.digiexpress.client.api.ImmutableRefIdValue;
import io.digiexpress.client.api.ImmutableServiceDefinitionDocument;
import io.digiexpress.client.api.ImmutableServiceRevisionDocument;
import io.digiexpress.client.api.ImmutableServiceRevisionValue;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposerCommand.CreateMigration;
import io.digiexpress.client.api.ServiceComposerState.MigrationState;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceStore;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.digiexpress.client.spi.support.TableLog;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CreateMigrationVisitor {
  private final CreateMigration source;
  private final ServiceClient client;
  private final LocalDateTime now = LocalDateTime.now();
  private final String tagNameMigrated = "migration";
  
  @Data @Builder(toBuilder = true) @Accessors(chain = true)
  private static class PartialState {
    private DialobClient.ProgramEnvir dialobEnvir;
    private DialobStore.StoreState dialobState;
    private StencilComposer.SiteState stencilState;
    private HdesStore.StoreState hdesState;
    private io.resys.hdes.client.api.programs.ProgramEnvir hdesEnvir;
    private ServiceStore.StoreState serviceState;
  } 
  
  public Uni<MigrationState> visit() {
  
    return visitForms(PartialState.builder().build())
        .onItem().transformToUni(this::visitStencil)
        .onItem().transformToUni(this::visitHdes)
        .onItem().transformToUni(this::visitService)
        .onItem().transform(partial -> {
          visitLog(partial);
          return ImmutableMigrationState.builder().build();
        });
  }
  
  private void visitLog(PartialState partial) {
    final var summary = new TableLog("asset type", "name", "params", "asset id");
    
    // stencil logs
    final var start = partial.getStencilState();
    final var childArticles = start.getArticles().values()
      .stream().filter(v -> v.getBody().getParentId() != null)
      .collect(Collectors.groupingBy(e -> e.getBody().getParentId()));

    final var pages = start.getPages().values().stream()
        .collect(Collectors.groupingBy(e -> e.getBody().getArticle()));
    final Function<String, String> getPageLocales = (articleId) -> {
      final var locales = pages.getOrDefault(articleId, Collections.emptyList()).stream()
          .map(page -> start.getLocales().get(page.getBody().getLocale())).map(e -> e.getBody().getValue())
          .collect(Collectors.toList());
      return String.join(",", locales);
    };
    final Function<String, String> getWkLocales = (wkId) -> {
      final var locales = start.getWorkflows().get(wkId).getBody().getLabels().stream()
          .map(page -> start.getLocales().get(page.getLocale())).map(e -> e.getBody().getValue())
          .collect(Collectors.toList());
      return String.join(",", locales);
    };    
    
    start.getArticles().values().stream()
    .filter(v -> v.getBody().getParentId() == null)
    .sorted((a, b) -> b.getBody().getName().compareTo(a.getBody().getName()))
    .forEach(value -> {
      summary.addRow(value.getType(), value.getBody().getName(), getPageLocales.apply(value.getId()), value.getId());
      
      for(final var childArticle : childArticles.getOrDefault(value.getId(), Collections.emptyList())) {
        summary.addRow(childArticle.getType(), "-->" + childArticle.getBody().getName(), getPageLocales.apply(childArticle.getId()), value.getId());
      }
    });
  
    start.getWorkflows().values().stream()
    .sorted((a, b) -> b.getBody().getValue().compareTo(a.getBody().getValue()))
    .forEach(value -> {
      summary.addRow(value.getType(), value.getBody().getValue(), getWkLocales.apply(value.getId()), value.getId());
    });
    start.getReleases().values().stream()
    .sorted((a, b) -> b.getBody().getName().compareTo(a.getBody().getName()))
    .forEach(value -> {
      summary.addRow(value.getType(), value.getBody().getName(), "", value.getId());
    });
    
    
    // dialob logs
    final var visitedDialobs = new ArrayList<String>();
    partial.getDialobEnvir().getValues().values().stream()
      .sorted((a, b) -> b.getDocument().getName().compareTo(a.getDocument().getName()))
      .forEach(value -> {
        final var doc = value.getDocument();
        if(doc instanceof FormDocument) {
          final var formDoc = (FormDocument) doc;
          final var content = client.getConfig().getDialob().getConfig().getMapper().toStoreBody(formDoc);
          if(visitedDialobs.contains(content)) {
            return;
          }
          visitedDialobs.add(content);
          summary.addRow(doc.getType(), formDoc.getName(), "", doc.getId());
        }
      });
    

    // hdes logs
    partial.getHdesEnvir().getValues().values().stream()
    .sorted((a, b) -> b.getType().compareTo(a.getType()))
    .forEach((wrapper) -> {
      summary.addRow(wrapper.getType(), wrapper.getAst().map(a -> a.getName()).orElse(wrapper.getId()), "", wrapper.getId());          
    });

    // service logs
    partial.getServiceState().getConfigs().values().forEach(wrapper -> {
      final var config = client.getConfig().getMapper().toConfig(wrapper);
      summary.addRow(
          wrapper.getBodyType(), 
          config.getHdes().getType(),
          config.getHdes().getId(),
          wrapper.getId());
      summary.addRow(
          wrapper.getBodyType(), 
          config.getStencil().getType(),
          config.getStencil().getId(),
          wrapper.getId());
      summary.addRow(
          wrapper.getBodyType(), 
          config.getDialob().getType(),
          config.getDialob().getId(),
          wrapper.getId());
      summary.addRow(
          wrapper.getBodyType(), 
          config.getService().getType(),
          config.getService().getId(),
          wrapper.getId());
    });

    final var visitedDefs = new ArrayList<ServiceDefinitionDocument>();
    partial.getServiceState().getDefs().values().forEach(wrapper -> {
      final var def = client.getConfig().getMapper().toDef(wrapper);
      visitedDefs.add(def);
      summary.addRow(
          wrapper.getBodyType(), 
          "Rev: " + wrapper.getId(),
          "No of processes: " + def.getProcesses().size(),
          wrapper.getId());
    });
    
    final var visitedProcessValues = new ArrayList<String>();
    final var workflowsByName = partial.getStencilState().getWorkflows().values().stream().collect(Collectors.toMap(e -> e.getBody().getValue(), e -> e));
    visitedDefs.stream().forEach(def -> {
      for(final var process : def.getProcesses()) {
        final var content = process.toString();
        if(visitedProcessValues.contains(content)) {
          continue;
        }
        visitedProcessValues.add(content);
        summary.addRow(
            "SERVICE_PROCESS", 
            process.getName(),
            "FLOW: " + (partial.getHdesState().getFlows().containsKey(process.getFlowId()) ? "OK     " : "MISSING") + " " + 
            "DIALOB: " + (partial.getDialobState().getForms().containsKey(process.getFormId()) ? "OK     " : "MISSING") + " " +
            "STENCIL: " + (workflowsByName.containsKey(process.getName()) ? "OK     " : "UNUSED ") + " ",
            process.getId());
          
      }
    });
    
    log.info("Migration state: " + summary.toString());    
  }

  private Uni<PartialState> visitService(PartialState partial) {
    
    return client.getQuery().getConfigDoc().onItem().transformToUni(configDoc -> {
      final var defMigrated = ImmutableServiceDefinitionDocument.builder()
        .created(now)
        .updated(now)
        .id(client.getConfig().getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_DEF))
        .addRefs(ImmutableRefIdValue.builder().repoId(configDoc.getStencil().getId()).tagName(tagNameMigrated).type(ConfigType.STENCIL).build())
        .addRefs(ImmutableRefIdValue.builder().repoId(configDoc.getHdes().getId()).tagName(tagNameMigrated).type(ConfigType.HDES).build())
        .processes(source.getServices().getProcesses())
        .build();
      
      final var defMain = ImmutableServiceDefinitionDocument.builder()
          .created(now)
          .updated(now)
          .id(client.getConfig().getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_DEF))
          .addRefs(ImmutableRefIdValue.builder().repoId(configDoc.getStencil().getId()).tagName(ServiceAssert.BRANCH_MAIN).type(ConfigType.STENCIL).build())
          .addRefs(ImmutableRefIdValue.builder().repoId(configDoc.getHdes().getId()).tagName(ServiceAssert.BRANCH_MAIN).type(ConfigType.HDES).build())
          .processes(source.getServices().getProcesses())
          .build();
    
      final var rev = ImmutableServiceRevisionDocument.builder()
          .id(client.getConfig().getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_REV))
          .created(now)
          .updated(now)
          .head(defMain.getId())
          .name("migrated-service")
          .addValues(ImmutableServiceRevisionValue.builder()
              .id(client.getConfig().getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_REV))
              .created(now)
              .updated(now)
              .revisionName(tagNameMigrated)
              .defId(defMigrated.getId())
              .build())
          .build();

      return client.getConfig().getStore().batch(Arrays.asList(
          io.digiexpress.client.api.ImmutableCreateStoreEntity.builder()
            .id(rev.getId())
            .version(rev.getVersion())
            .bodyType(rev.getType())
            .body(client.getConfig().getMapper().toBody(rev))
            .build(),
          io.digiexpress.client.api.ImmutableCreateStoreEntity.builder()
            .id(defMain.getId())
            .version(defMain.getVersion())
            .bodyType(defMain.getType())
            .body(client.getConfig().getMapper().toBody(defMain))
            .build(),
          io.digiexpress.client.api.ImmutableCreateStoreEntity.builder()
            .id(defMigrated.getId())
            .version(defMigrated.getVersion())
            .bodyType(defMigrated.getType())
            .body(client.getConfig().getMapper().toBody(defMigrated))
            .build()
          ))
          .onItem().transformToUni(created -> client.getQuery().head())
          .onItem().transform(state -> partial.toBuilder().serviceState(state).build());
    });
  }
  
  private Uni<PartialState> visitStencil(PartialState partial) {
    final var state = source.getStencil();
    final var store = client.getConfig().getStencil().getStore();
    final var batch = new ArrayList<Entity<?>>();
    state.getLocales().values().forEach(batch::add);
    state.getArticles().values().forEach(batch::add);
    state.getPages().values().forEach(batch::add);
    state.getWorkflows().values().forEach(batch::add);
    state.getLinks().values().forEach(batch::add);
    state.getTemplates().values().forEach(batch::add);
    state.getReleases().values().forEach(batch::add);
    
    return store.saveAll(batch)
        .onItem().transformToUni((saved) -> {
          final var composer = new StencilComposerImpl(client.getConfig().getStencil());
          final var release = io.thestencil.client.api.ImmutableCreateRelease.builder()
              .name(tagNameMigrated)
              .build();
          return composer.create().release(release);
        })
        .onItem().transformToUni(created -> store.query().head())
        .onItem().transform(head -> partial.toBuilder().stencilState(head).build());
  }

  private Uni<PartialState> visitHdes(PartialState partial) {
    final var store = client.getConfig().getHdes().store();
    final var batch = new ArrayList<HdesStore.CreateStoreEntity>();
    final var state = source.getHdes();
    
    state.getDecisions().values().stream()
      .map(e -> io.resys.hdes.client.api.ImmutableCreateStoreEntity.builder()
          .bodyType(e.getBodyType())
          .body(e.getBody())
          .build())
      .forEach(batch::add);
    
    state.getServices().values().stream()
      .map(e -> io.resys.hdes.client.api.ImmutableCreateStoreEntity.builder()
        .id(e.getId())
        .bodyType(e.getBodyType())
        .body(e.getBody())
        .build())
      .forEach(batch::add);
    
    state.getFlows().values().stream()
      .map(e -> io.resys.hdes.client.api.ImmutableCreateStoreEntity.builder()
        .id(e.getId())
        .bodyType(e.getBodyType())
        .body(e.getBody())
        .build())
      .forEach(batch::add);
    
    state.getTags().values().stream()
      .map(e -> io.resys.hdes.client.api.ImmutableCreateStoreEntity.builder()
        .id(e.getId())
        .bodyType(e.getBodyType())
        .body(e.getBody())
        .build())
      .forEach(batch::add);
    
    return store.batch(ImmutableImportStoreEntity.builder().create(batch).build())
        .onItem().transformToUni(created -> {
          final var composer = new HdesComposerImpl(client.getConfig().getHdes());
          final var newTag = io.resys.hdes.client.api.ImmutableCreateEntity.builder()
              .name(tagNameMigrated)
              .desc("")
              .type(AstBodyType.TAG)
              .build();
          return composer.create(newTag);
        })
        .onItem().transformToUni(created -> store.query().get())
        .onItem().transform(head -> {
          final var envir = client.getConfig().getHdes().envir();
          for(final var entity : head.getDecisions().values()) {
            envir.addCommand().decision(entity).id(entity.getId()).build();
          }
          for(final var entity : head.getFlows().values()) {
            envir.addCommand().flow(entity).id(entity.getId()).build();
          }
          for(final var entity : head.getServices().values()) {
            envir.addCommand().service(entity).id(entity.getId()).build();
          }
          for(final var entity : head.getTags().values()) {
            envir.addCommand().tag(entity).id(entity.getId()).build();
          }
          return partial.toBuilder().hdesState(head).hdesEnvir(envir.build()).build();
        });
  }
  
  private Uni<PartialState> visitForms(PartialState partial) {
    final var summary = new TableLog("asset type", "name", "asset id", "event type", "description", "args");
    final var mapper = client.getConfig().getDialob().getConfig().getMapper();
    final var store = client.getConfig().getDialob().getConfig().getStore();
    final var forms = new HashMap<>(source.getForms().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    final var batch = new ArrayList<StoreCommand>();
    for(final var startRev : source.getFormRevs()) {

      // Create head if not present
      String head = startRev.getHead();
      if(!forms.containsKey(head)) {
        final var lastRev = startRev.getEntries().stream().sorted((b, a) -> b.getCreated().compareTo(a.getCreated())).findFirst().get();
        head = OidUtils.gen();
        summary.addRow(DialobDocument.DocumentType.FORM, startRev.getName(), head, "CREATE-SNAPSHOT", "Snapshot from last rev", "lastRev: " + lastRev.getRevisionName());
        final var form = ImmutableFormDocument.builder().from(forms.get(lastRev.getFormId()))
            .id(head)
            .version(null)
            .build();
        forms.put(form.getId(), form);
      }
      
      final var rev = ImmutableFormRevisionDocument.builder()
          .from(startRev)
          .head(head)
          .build();

      final var formids = new ArrayList<>();
      formids.add(head);
      rev.getEntries().forEach(e -> formids.add(e.getFormId()));
      
      for(final var formId : formids) {
        final var form = forms.get(formId);
        final var createForm = io.dialob.client.api.ImmutableCreateStoreEntity.builder()
            .body(mapper.toStoreBody(form))
            .bodyType(DialobDocument.DocumentType.FORM)
            .id(form.getId())
            .version(form.getVersion())
            .build();
        batch.add(createForm);
      }
      
      final var createRev = io.dialob.client.api.ImmutableCreateStoreEntity.builder()
          .body(mapper.toStoreBody(rev))
          .bodyType(DialobDocument.DocumentType.FORM_REV)
          .id(rev.getId())
          .version(rev.getVersion())
          .build();
      batch.add(createRev);
    }
    log.info(summary.toString());
    return store.batch(batch)
        .onItem().transformToUni(created -> store.query().get())
        .onItem().transform(state -> {
          final var envir = client.getConfig().getDialob().envir().from(state).build();
          return PartialState.builder().dialobEnvir(envir).dialobState(state).build();
        });
  }
}
