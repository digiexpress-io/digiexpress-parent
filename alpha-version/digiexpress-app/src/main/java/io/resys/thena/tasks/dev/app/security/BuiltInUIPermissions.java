package io.resys.thena.tasks.dev.app.security;

public enum BuiltInUIPermissions {
  UI_STENCIL_COMPOSER("Access stencil react app"),
  UI_WRENCH_COMPOSER("Access wrench react app"),
  UI_DIALOB_COMPOSER("Access dial react app"),
  UI_SYS_CONFIG_COMPOSER("Access sys config react app"),
  UI_PERMISSIONS_COMPOSER("Access permissions react app"),
  UI_PERMISSIONS_CHART("Access permission react app"),

  UI_PROFILE("Access user profile react app"),
  UI_CRM("Access crm admin react app"),
  UI_TASK_ADMIN("Access task admin react app"),
  UI_TASK_MY_WORK("Access my work react app");
  
  
  private final String description;
  BuiltInUIPermissions(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description; 
  }
}
