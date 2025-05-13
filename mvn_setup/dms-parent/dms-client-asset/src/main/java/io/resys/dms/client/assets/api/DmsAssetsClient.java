package io.resys.dms.client.assets.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import io.smallrye.mutiny.Uni;

/**
 * document management system.
 * 
 * Provides virtual hard-drive
 */
public interface DmsAssetsClient {
  
  interface DmsAssetMetaQuery {
    Uni<AnyAsset> findAll();
  }
  
  
  interface AnyAsset {
    String getId();
    String getName();
    DmsAssetFileType getType();
  }
  
  interface DmsAssetDirent {
    boolean isFile();
    boolean isFolder();
    
    OffsetDateTime getCreatedAt();
    String getCreatedBy();
    List<DmsAssetLabel> getLabels();
    
    String getName();
  }
  
  interface DmsAssetLabel {
    OffsetDateTime getCreatedAt();
    String getCreatedBy();
    String getValue();
  }

  
  interface DmsAssetFile extends DmsAssetDirent {
    String getId(); // internal GID

    String getParentId();
    DmsAssetFileType getFileType();
    String getExternalId();
  }
  
  interface DmsFolder extends DmsAssetDirent {
    String getId(); // internal GID
    Optional<String> getParentId(); 
  }
  
  enum DmsAssetFileType {
    DIALOB_FORM,

    STENCIL_LOCALE,
    STENCIL_LINK,
    STENCIL_ARTICLE,
    STENCIL_WORKFLOW,
    STENCIL_PAGE,

    WRENCH_DT,
    WRENCH_FL,
    WRENCH_ST
  }
}
