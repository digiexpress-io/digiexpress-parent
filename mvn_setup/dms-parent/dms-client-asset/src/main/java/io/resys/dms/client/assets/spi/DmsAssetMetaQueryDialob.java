//package io.resys.dms.client.assets.spi;
//
//import java.time.OffsetDateTime;
//import java.time.ZoneId;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import io.digiexpress.eveli.client.web.resources.assets.AssetsDialobController.FormTagResult;
//import io.digiexpress.eveli.dialob.api.DialobClient;
//import io.resys.dms.client.assets.api.DmsAssetsClient.AnyAsset;
//import io.resys.dms.client.assets.api.DmsAssetsClient.DmsAssetFileType;
//import io.resys.dms.client.assets.api.DmsAssetsClient.DmsAssetMetaQuery;
//import io.resys.dms.client.assets.api.ImmutableDmsAssetFile;
//import io.smallrye.mutiny.Uni;
//import jakarta.validation.constraints.NotNull;
//import lombok.RequiredArgsConstructor;
//
//
//@RequiredArgsConstructor
//public class DmsAssetMetaQueryDialob implements DmsAssetMetaQuery {
//  private final DialobClient dialobClient;
//  
//  @Override
//  public Uni<AnyAsset> findAll() {
//    dialobClient.findAllForms().stream().collect(Collectors.toMap(form -> form.getId(), form -> form));
//    
//    final var tags = dialobClient.findAllFormTags().stream().collect(Collectors.groupingBy(e -> e.getFormName()));
//    
//    for(final var tag : tags.entrySet()) {
//      final var formName = tag.getKey();
//      final var formTags = tag.getValue().stream().sorted((a, b) -> a.getCreated().compareTo(b.getCreated())).toList();
//      final var firstFormTag = formTags.iterator().next();
//      firstFormTag.get
//      
//      
//      ImmutableDmsAssetFile.builder()
//        .createdBy(firstFormTag.getMetadata().getCreator())
//        .createdAt(OffsetDateTime.ofInstant(form.getMetadata().getCreated().toInstant(), ZoneId.systemDefault()))
//        .externalId(formName)
//        .name(formName)
//        .fileType(DmsAssetFileType.DIALOB_FORM)
//        .labels(null)
//        .isFolder(false)
//        .isFile(true)
//        .build();
//      /
//    }
//    
//    
//
//    
//    
//    
//    final var forms = dialobCommands.findAllForms();
//    Map<String, @NotNull String> formLabels = forms.stream().collect(Collectors.toMap(val->val.getId(), val->val.getMetadata().getLabel()));
//    final var formTags = dialobCommands.findAllFormTags();
//    for (var formTag : formTags) {
//      FormTagResult result = new FormTagResult();
//      result.setFormName(formTag.getFormName());
//      result.setFormLabel(formLabels.get(formTag.getFormName()));
//      result.setTagFormId(formTag.getFormId());
//      result.setTagName(formTag.getName());
//      tags.add(result);
//    }
//    
//    return null;
//  }  
//}
//
//
