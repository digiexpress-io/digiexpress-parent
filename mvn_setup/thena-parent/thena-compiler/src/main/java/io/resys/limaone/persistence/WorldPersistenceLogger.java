package io.resys.limaone.persistence;

import io.resys.thena.fs.api.FileSystem.FileSystemTenant;
import io.resys.thena.fs.entities.Ref;

public class WorldPersistenceLogger {
  public void stage1TenantConfigured(FileSystemTenant tenant, String commitId) {
    
  }
  
  public void stage2CurrentState(Ref ref) {
    
  }
  public void stage3LockFailed(Ref ref) {
    
  }  
  
  public void stage4NextState() {
    
  }
  
  public void closeWithFailure(Throwable e) {
    
  }
  
  public void close() {
    
  }
}
