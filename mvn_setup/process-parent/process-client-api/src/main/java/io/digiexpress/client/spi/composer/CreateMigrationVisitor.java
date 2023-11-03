package io.digiexpress.client.spi.composer;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientEntity;
import io.digiexpress.client.api.ClientEntity.ConfigType;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientStore;
import io.digiexpress.client.api.ClientStore.ClientStoreCommand;
import io.digiexpress.client.api.ComposerEntity.CreateMigration;
import io.digiexpress.client.api.ComposerEntity.MigrationState;
import io.digiexpress.client.api.ImmutableMigrationState;
import io.digiexpress.client.api.ImmutableProject;
import io.digiexpress.client.api.ImmutableProjectRevision;
import io.digiexpress.client.api.ImmutableRefIdValue;
import io.digiexpress.client.api.ImmutableServiceDefinition;
import io.digiexpress.client.spi.support.MainBranch;
import io.digiexpress.client.spi.support.TableLog;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
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
  private final Client client;
  private final LocalDateTime now = LocalDateTime.now();
  private final String tagNameMigrated = "migration";
  
  @Data @Builder(toBuilder = true) @Accessors(chain = true)
  private static class PartialState {
    private DialobClient.ProgramEnvir dialobEnvir;
    private DialobStore.StoreState dialobState;
    private StencilComposer.SiteState stencilState;
    private HdesStore.StoreState hdesState;
    private io.resys.hdes.client.api.programs.ProgramEnvir hdesEnvir;
    private ClientStore.StoreState projectState;
  } 
  
  public Uni<MigrationState> visit() {
    final var projectId = Optional.ofNullable(source.getProjectId()).orElse(client.getQuery().getProjectDefaultId());
    
    return visitForms(PartialState.builder().build())
        .onItem().transformToUni(this::visitStencil)
        .onItem().transformToUni(this::visitHdes)
        .onItem().transformToUni(partial -> 
            client.getQuery().getProject(projectId).onItem()
              .transformToUni(project -> visitProject(partial, project))
        )
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
    partial.getProjectState().getProjects().values().forEach(wrapper -> {
      final var prj = client.getConfig().getParser().toProject(wrapper);
      final var config = prj.getConfig();
      
      summary.addRow(
          wrapper.getBodyType(), 
          ClientEntity.ConfigType.HDES,
          config.getHdes(),
          wrapper.getId());
      summary.addRow(
          wrapper.getBodyType(), 
          ClientEntity.ConfigType.STENCIL,
          config.getStencil(),
          wrapper.getId());
      summary.addRow(
          wrapper.getBodyType(), 
          ClientEntity.ConfigType.DIALOB,
          config.getDialob(),
          wrapper.getId());
      summary.addRow(
          wrapper.getBodyType(), 
          ClientEntity.ConfigType.PROJECT,
          prj.getId(),
          wrapper.getId());
    });

    final var visitedDefs = new ArrayList<ServiceDefinition>();
    partial.getProjectState().getDefinitions().values().forEach(wrapper -> {
      final var def = client.getConfig().getParser().toDefinition(wrapper);
      visitedDefs.add(def);
      summary.addRow(
          wrapper.getBodyType(), 
          "Rev: " + wrapper.getId(),
          "No of processes: " + def.getDescriptors().size(),
          wrapper.getId());
    });
    
    final var visitedProcessValues = new ArrayList<String>();
    final var workflowsByName = partial.getStencilState().getWorkflows().values().stream().collect(Collectors.toMap(e -> e.getBody().getValue(), e -> e));
    visitedDefs.stream().forEach(def -> {
      for(final var process : def.getDescriptors()) {
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

  private Uni<PartialState> visitProject(PartialState partial, Project project) {
    final var config = project.getConfig();
    final var gid = client.getConfig().getStore().getGid();
    
    final var defMigrated = ImmutableServiceDefinition.builder()
      .projectId(project.getId())
      .created(now)
      .updated(now)
      .id(gid.getNextId(ClientEntity.ClientEntityType.SERVICE_DEF))
      .addRefs(ImmutableRefIdValue.builder().repoId(config.getStencil()).tagName(tagNameMigrated).type(ConfigType.STENCIL).build())
      .addRefs(ImmutableRefIdValue.builder().repoId(config.getHdes()).tagName(tagNameMigrated).type(ConfigType.HDES).build())
      .descriptors(source.getServices().getDescriptors())
      .build();
    
    final var defMain = ImmutableServiceDefinition.builder()
      .projectId(project.getId())
      .created(now)
      .updated(now)
      .id(gid.getNextId(ClientEntity.ClientEntityType.SERVICE_DEF))
      .addRefs(ImmutableRefIdValue.builder().repoId(config.getStencil()).tagName(MainBranch.HEAD_NAME).type(ConfigType.STENCIL).build())
      .addRefs(ImmutableRefIdValue.builder().repoId(config.getHdes()).tagName(MainBranch.HEAD_NAME).type(ConfigType.HDES).build())
      .descriptors(source.getServices().getDescriptors())
      .build();

    final var rev = ImmutableProject.builder().from(project)
      .updated(now)
      .head(defMain.getId())
      .name("migrated-service")
      .addRevisions(ImmutableProjectRevision.builder()
          .id(gid.getNextId(ClientEntity.ClientEntityType.PROJECT))
          .created(now)
          .updated(now)
          .revisionName(tagNameMigrated)
          .defId(defMigrated.getId())
          .build())
      .build();

    final var parser = client.getConfig().getParser();
    final List<ClientStoreCommand> commands = Arrays.asList(
        io.digiexpress.client.api.ImmutableCreateStoreEntity.builder()
        .id(rev.getId())
        .version(rev.getVersion())
        .bodyType(rev.getType())
        .body(parser.toStore(rev))
        .build(),
      io.digiexpress.client.api.ImmutableCreateStoreEntity.builder()
        .id(defMain.getId())
        .version(defMain.getVersion())
        .bodyType(defMain.getType())
        .body(parser.toStore(defMain))
        .build(),
      io.digiexpress.client.api.ImmutableCreateStoreEntity.builder()
        .id(defMigrated.getId())
        .version(defMigrated.getVersion())
        .bodyType(defMigrated.getType())
        .body(parser.toStore(defMigrated))
        .build()
      );
    return client.getConfig().getStore().batch(commands)
      .onItem().transformToUni(created -> client.getQuery().getProjectHead())
      .onItem().transform(state -> partial.toBuilder().projectState(state).build());
  }
  
  private Uni<PartialState> visitStencil(PartialState partial) {
    final var state = source.getStencil();
    final var store = client.getConfig().getStencil().getStore();
    final var batch = new ArrayList<StencilClient.Entity<?>>();
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
