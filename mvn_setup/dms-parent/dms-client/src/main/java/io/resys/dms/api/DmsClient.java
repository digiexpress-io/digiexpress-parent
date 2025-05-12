package io.resys.dms.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * document management system.
 * 
 * Provides virtual hard-drive
 */
public interface DmsClient {

  interface Dirent {
    boolean isFile();
    boolean isFolder();
  }
  
  interface DmsLabel {
    OffsetDateTime getCreatedAt();
    String getCreatedBy();
    String getValue();
  }

  interface DmsFileMeta {
    String getExternalId();
    OffsetDateTime getCreatedAt();
    String getCreatedBy();
    
    List<DmsLabel> getLabels();
  }
  
  interface DmsFile extends Dirent {
    String getId(); // internal GID

    String getParentId();
    String getFileName();
    String getFileType();
    
    DmsFileMeta getFileMeta();
  }
  
  
  interface DmsFolderMeta {
    OffsetDateTime getCreatedAt();
    String getCreatedBy();
    List<DmsLabel> getLabels();
  }
  interface DmsFolder extends Dirent {
    String getId(); // internal GID
    DmsFolderMeta getMeta();
    Optional<String> getParentId(); 
  }
}
