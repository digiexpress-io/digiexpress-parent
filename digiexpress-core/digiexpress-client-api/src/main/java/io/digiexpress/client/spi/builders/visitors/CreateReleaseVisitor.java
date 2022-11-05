
package io.digiexpress.client.spi.builders.visitors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.CompressionMapper;
import io.digiexpress.client.api.ImmutableServiceReleaseDocument;
import io.digiexpress.client.api.ImmutableServiceReleaseValue;
import io.digiexpress.client.api.QueryFactory;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.ProcessValue;
import io.digiexpress.client.api.ServiceDocument.RefIdValue;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseValue;
import io.digiexpress.client.spi.support.ReleaseException;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.thena.docdb.spi.commits.Sha2;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateReleaseVisitor {
  
  private final ServiceClientConfig config;
  private final QueryFactory query;
  private final LocalDateTime now;
  
  
  public Uni<ServiceReleaseDocument> visit(final ServiceDefinitionDocument def, final String name, final LocalDateTime activeFrom) {
    return Multi.createFrom().items(def.getStencil(), def.getHdes())
      .onItem().transformToUni(this::visitRef).concatenate().collect().asList()
      .onItem().transformToUni(this::visitResolvedRef)
      .onItem().transformToUni((ResolvedAssetEnvir envir) -> {
        
        // resolve forms
        final Uni<List<ResolvedAsset>> resolvedForms = Multi.createFrom().items(def.getProcesses().stream())
            .onItem().transformToUni(process -> query.getForm(process.getFormId())
            .onItem().transform(form -> visitForm(process, form.getData(), envir)))
            .concatenate().collect().asList();
        
        // combine all resolved assets
        return resolvedForms.onItem().transform(forms -> Stream.of(forms, envir.getResolved()).flatMap(e -> e.stream()));
      })
      .onItem().transformToMulti(resp -> Multi.createFrom().items(resp))
      .onItem().transform(this::visitCompression)
      .collect().asList().onItem().transform((List<CompressedAsset> compressed) -> {
        return ImmutableServiceReleaseDocument.builder()
            .name(name)
            .desc(visitDesc(def, compressed, name, activeFrom))
            .repoId(config.getStore().getRepoName())
            .created(now)
            .updated(now)
            .activeFrom(now)
            .values(compressed.stream().map(e -> e.getValue()).collect(Collectors.toList()))
            .build();
      });
  }
  
  protected List<String> visitDesc(ServiceDefinitionDocument def, List<CompressedAsset> compressed, String name, LocalDateTime activeFrom) {
    final var compressinsById = new HashMap<String, CompressedAsset>();
    final var forms = new HashMap<String, ResolvedAssetForm>();
    final var flows = new HashMap<String, AstFlow>();
    ResolvedAssetHdes hdes = null; 
    ResolvedAssetStencil stencil = null; 
    
    for(final var comp : compressed) {
      if(comp.getSource() instanceof ResolvedAssetForm) {
        ResolvedAssetForm asset = (ResolvedAssetForm) comp.getSource(); 
        forms.put(asset.getForm().getId(), asset);
        compressinsById.put(asset.getForm().getId(), comp);
        
      } else if(comp.getSource() instanceof ResolvedAssetHdes) {
        ResolvedAssetHdes asset = (ResolvedAssetHdes) comp.getSource();
        hdes = asset;
        asset.getEnvir().getFlowsByName().values().stream()
        .filter(program -> program.getAst().isPresent())
        .forEach(program -> {
          flows.put(program.getId(), program.getAst().get());
        });
      } else if(comp.getSource() instanceof ResolvedAssetStencil) {
        ResolvedAssetStencil asset = (ResolvedAssetStencil) comp.getSource();
        stencil = asset;
      }
    }
    
    final var flCount = hdes.getAstTag().getValues().stream().filter(c -> c.getBodyType() == AstBodyType.FLOW).count();
    final var dtCount = hdes.getAstTag().getValues().stream().filter(c -> c.getBodyType() == AstBodyType.DT).count();
    final var stCount = hdes.getAstTag().getValues().stream().filter(c -> c.getBodyType() == AstBodyType.FLOW_TASK).count();
    
    final var desc = new ArrayList<String>();
    desc.addAll(Arrays.asList(
      "# Release document",
      "name: '" + name + "', created at: '" + now + "', active from: '" + activeFrom + "'"));

    desc.addAll(Arrays.asList(
        "# Stencil description",
        "Release is create from tag: '" + stencil.getTagName() + "'", 
        "There are total of: '" + stencil.getSites().getSites().size() + "' locale(s)"));

    for(final var site : stencil.getSites().getSites().values()) {
      desc.addAll(Arrays.asList(
          "  - locale: '" +site.getLocale() + "'",
          "     total of: '" + site.getTopics().size() + "' topics",
          "     total of: '" + site.getLinks().values().stream().filter(t -> t.getWorkflow()).count() + "' workflows",
          "     total of: '" + site.getLinks().values().stream().filter(t -> !t.getWorkflow()).count() + "' links"
          ));
    }
    
    desc.addAll(Arrays.asList(
      "# Hdes description",
      "Release is create from tag: '" + hdes.getAstTag().getName() + "'", 
      "  total of: '" + flCount + "' flows",
      "  total of: '" + dtCount + "' decision tables",
      "  total of: '" + stCount + "' service tasks"));

    
    desc.addAll(Arrays.asList(
      "# Process descriptions",
      "There are total of: '" + def.getProcesses().size() + "' entries"));
    
    for(final var process : def.getProcesses()) {
      final var form = forms.get(process.getFormId());
      final var flow = flows.get(process.getFlowId());
      
      desc.addAll(Arrays.asList(
      " - process name: '" + process.getName() + "', id: '" + process.getId() + "'",
      "     flow id: '" + process.getFlowId() + "'",
      "     flow name: '" + flow.getName() + "'",
      "     flow hash: '" + Sha2.blobId(flow.getSrc().getValue()) + "'",
      "     form id: '" + process.getFormId() + "'",
      "     form name: '" + form.getForm().getName() + "'",
      "     form hash: '" + compressinsById.get(form.getForm().getId()).getTarget().getHash() + "'"));
    }
    
    return desc;
  }
  
  protected void visitArticles(Sites sites) {

  }
  protected void visitFlow(AstFlow flow) {
    
  }
  protected void visitFlowTask(AstService service) {
    
  }
  protected void visitDecisionTable(AstDecision decision) {
    
  }
  protected ResolvedAsset visitForm(ProcessValue process, Form form, ResolvedAssetEnvir envir) {
    return ImmutableResolvedAssetForm.builder().form(form).build();
  }
  protected CompressedAsset visitCompression(ResolvedAsset source) {
    final var mapper = this.config.getCompression();
    final var releaseValue = ImmutableServiceReleaseValue.builder();
    final CompressionMapper.Compressed target;
    if(source instanceof ResolvedAssetHdes) {
      final var tag = ((ResolvedAssetHdes) source).getAstTag();
      target = mapper.compress(tag);
      releaseValue.bodyType(ConfigType.HDES).id(tag.getName());
    } else if(source instanceof ResolvedAssetStencil) {
      final var sites = (ResolvedAssetStencil) source;
      target = mapper.compress(sites.getSites());
      releaseValue.bodyType(ConfigType.STENCIL).id(sites.getTagName());
    } else if(source instanceof ResolvedAssetForm) {
      final var form = ((ResolvedAssetForm) source).getForm();
      target = mapper.compress(form);
      releaseValue.bodyType(ConfigType.DIALOB).id(form.getId());
    } else {
      throw new ReleaseException("Unknown compression asset type: " + source.getClass().getSimpleName());
    }
    return ImmutableCompressedAsset.builder()
        .source(source).target(target)
        .value(releaseValue.body(target.getValue()).bodyHash(target.getHash()).build())
        .build();
  }
  protected Uni<ResolvedAsset> visitRef(RefIdValue value) {
    if(value.getType() == ConfigType.HDES) {
      return query.getHdes(value.getTagName()).onItem().transform(ast -> {
        final var envir = config.getHdes().envir();
        ast.getValues().forEach(asset -> {
          final var entity = io.resys.hdes.client.api.ImmutableStoreEntity.builder()
            .bodyType(asset.getBodyType())
            .body(asset.getCommands())
            .id(asset.getId())
            .hash(asset.getHash())
            .build();
          if(asset.getBodyType() == AstBodyType.FLOW) {
            envir.addCommand().id(asset.getId()).flow(entity).build();
          } else if(asset.getBodyType() == AstBodyType.FLOW_TASK) {
            envir.addCommand().id(asset.getId()).service(entity).build();
          } else if(asset.getBodyType() == AstBodyType.DT) {
            envir.addCommand().id(asset.getId()).decision(entity).build();
          }
        });
        return ImmutableResolvedAssetHdes.builder().astTag(ast).envir(envir.build()).build();
      });
      
    } else if(value.getType() == ConfigType.STENCIL) {
      return query.getStencil(value.getTagName()).onItem()
          .transform(sites -> ImmutableResolvedAssetStencil.builder().tagName(value.getTagName()).sites(sites).build());
    }
    throw new ReleaseException("Unknown ref type: " + value.getType());
  }
  
  protected Uni<ResolvedAssetEnvir> visitResolvedRef(List<ResolvedAsset> refs) {
    final var stencil = refs.stream()
        .filter(r -> r instanceof ResolvedAssetStencil)
        .map(r -> (ResolvedAssetStencil) r)
        .findFirst();
    final var hdes = refs.stream()
        .filter(r -> r instanceof ResolvedAssetHdes)
        .map(r -> (ResolvedAssetHdes) r)
        .findFirst();
    return Uni.createFrom().item(ImmutableResolvedAssetEnvir.builder()
        .stencil(stencil.get())
        .hdes(hdes.get())
        .build());
  }

  public interface ResolvedAsset {}

  @Value.Immutable
  public interface ResolvedAssetStencil extends ResolvedAsset {
    String getTagName();
    Sites getSites();
  }
  @Value.Immutable
  public interface ResolvedAssetHdes extends ResolvedAsset {
    AstTag getAstTag();
    io.resys.hdes.client.api.programs.ProgramEnvir getEnvir();
  }  
  @Value.Immutable
  public interface ResolvedAssetForm extends ResolvedAsset {
    Form getForm();
  }
  @Value.Immutable
  public interface ResolvedAssetEnvir {
    ResolvedAssetStencil getStencil();
    ResolvedAssetHdes getHdes();
    default List<ResolvedAsset> getResolved() {
      return Arrays.asList(getStencil(), getHdes());
    };
  }
  
  @Value.Immutable
  public interface CompressedAsset {
    ResolvedAsset getSource();
    CompressionMapper.Compressed getTarget();
    ServiceReleaseValue getValue();
  }
  
}