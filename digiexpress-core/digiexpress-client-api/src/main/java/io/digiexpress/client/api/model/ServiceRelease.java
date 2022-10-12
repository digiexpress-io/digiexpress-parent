package io.digiexpress.client.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.model.ServiceDef.ActivityDef;
import io.resys.hdes.client.api.ast.AstFlow;
import io.thestencil.client.api.StencilClient;

public interface ServiceRelease extends Serializable {
  String getId();
  String getHash();
  String getReleaseName();
  LocalDateTime getActiveFrom();
  
  List<FormRelease> getForms();
  List<ActivityRelease> getActivities();
  List<FlowRelease> getFlows();
  List<ArticlesRelease> getArticles();
  
  
  interface FormRelease extends ReleaseItem<Form> {
    String getFormTagName();
    String getFormTechnicalName();
    String getFormGid();
    
  }
  
  interface ActivityRelease extends ReleaseItem<ActivityDef> {
    String getActivityName();
    String getActivityTagName();
  }
  
  interface FlowRelease extends ReleaseItem<AstFlow> {
    String getFlowName();
    String getTagName();
  }
  interface ArticlesRelease extends ReleaseItem<StencilClient.Release> {
    String getTagName();
  }
  
  
  interface ReleaseItem<T> extends Serializable {
    String getHash();
    String getSrc();
    ReleaseItemType getType();
    
    @JsonIgnore
    T getEntity();    
  }
  
  enum ReleaseItemType {
    FORM, ACTIVITY, INTL, FLOW, ARTICLES
  }
}
