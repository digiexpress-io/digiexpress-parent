package io.digiexpress.client.api.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceDef {
  String getId();
  String getVersion();
  
  String getName(); // mutable
  String getDesc(); // mutable

  LocalDateTime getCreated();
  LocalDateTime getUpdated();
  ServiceDefBody getBody();

  
  interface ServiceDefBody {
    ServiceDefConfig getConfig();
    
    List<ActivityDef> getActivities();
    List<FlowDef> getFlows();
    List<FormDef> getForms();
    List<ArticleDef> getArticles();
    
    List<Intl> getIntl();
  }
  
  interface FlowDef {
    String getId();
  }

  interface FormDef {
    String getId();
  }
  
  interface ArticleDef {
    String getId();
  }

  
  interface ActivityDefRevision {
    String getId();
    String getVersion();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    
    String getActivityName();
    String getActivityDesc();
    
    ActivityDef getHead();
    List<ActivityDef> getRevisions();
  }
  
  interface ActivityDef {
    String getId();
    String getRevisionName();
    StencilLink getStencil();
    
    Optional<IntlLink> getIntl();
    Optional<DialobLink> getDialob();
    Optional<WrenchLink> getWrench();
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
