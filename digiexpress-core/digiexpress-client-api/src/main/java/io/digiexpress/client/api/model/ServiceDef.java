package io.digiexpress.client.api.model;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceDef {
  String getId();
  String getVersion();
  
  String getName(); // mutable
  String getDesc(); // mutable
  ServiceDefConfig getConfig();
  
  LocalDateTime getCreated();
  LocalDateTime getUpdated();
  
  List<Activity> getActivities();
  List<Intl> getIntl();

  
  interface ServiceDefConfig {
    ProjectConfig getMain();
    ProjectConfig getStencil();
    ProjectConfig getDialob();
    ProjectConfig getWrench();
  }
  
  interface ProjectConfig {
    String getId();
  }

  interface Activity {
    String getId();
    String getVersion();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    
    String getActivityName();
    String getActivityDesc();
    
    ActivityRevision getHead();
    List<ActivityRevision> getRevisions();
  }
  
  interface ActivityRevision {
    String getId();
    String getRevisionName();
    
    IntlLink getIntl();
    DialobLink getDialob();
    WrenchLink getWrench();
    StencilLink getStencil();
  }
  
  interface Intl {
    String getId();
    List<IntlRevision> getRevisions();    
  }
  
  interface IntlRevision {
    String getId();
    String getRevisionName();
  }
  
  interface IntlLink {
    String getIntlId();
    String getTagName();    
  }
  
  interface DialobLink {
    String getFormName();
    String getTagName();
  }
  
  interface WrenchLink {
    String getFlowName();
    String getTagName();
  }
  
  interface StencilLink {
    String getTagName();
  }
}
