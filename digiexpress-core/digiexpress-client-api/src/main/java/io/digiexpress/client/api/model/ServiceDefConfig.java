package io.digiexpress.client.api.model;

public interface ServiceDefConfig {

  ProjectConfig getService();
  ProjectConfig getStencil();
  ProjectConfig getDialob();
  ProjectConfig getWrench();

  
  interface ProjectConfig {
    String getId();
  }
}
