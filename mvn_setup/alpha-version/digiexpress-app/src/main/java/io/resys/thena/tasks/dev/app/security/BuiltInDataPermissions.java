package io.resys.thena.tasks.dev.app.security;

public enum BuiltInDataPermissions {
  
  DATA_TENANT_READ("Enables querying of TENANT data using rest api"),
  DATA_TENANT_WRITE("Enables update of TENANT data using rest api"),
  DATA_TENANT_DELETE("Enables deletion of TENANT data using rest api"),
  
  
  DATA_STENCIL_READ("Enables querying of stencil data using rest api"),
  DATA_STENCIL_WRITE("Enables update of stencil data using rest api"),
  DATA_STENCIL_DELETE("Enables deletion of stencil data using rest api"),
  
  DATA_WRENCH_READ("Enables querying of wrench data using rest api"),
  DATA_WRENCH_WRITE("Enables update of wrench data using rest api"),
  DATA_WRENCH_DELETE("Enables deletion of wrench data using rest api"),
  
  DATA_DIALOB_READ("Enables querying of dialob data using rest api"),
  DATA_DIALOB_WRITE("Enables update of dialob data using rest api"),
  DATA_DIALOB_DELETE("Enables deletion of dialob data using rest api"),
  
  DATA_TASKS_READ("Enables querying of task data using rest api"),
  DATA_TASKS_WRITE("Enables update of task data using rest api"),
  DATA_TASKS_DELETE("Enables deletion of task data using rest api"),
  
  DATA_CRM_READ("Enables querying of crm data using rest api"),
  DATA_CRM_WRITE("Enables update of crm data using rest api"),
  DATA_CRM_DELETE("Enables deletion of crm data using rest api"),
  
  
  DATA_PERMISSIONS_READ("Enables querying of permission data using rest api"),
  DATA_PERMISSIONS_WRITE("Enables update of permission data using rest api"),
  DATA_PERMISSIONS_DELETE("Enables deletion of permission data using rest api"),

  DATA_SYSCONFIG_READ("Enables querying of release management data using rest api"),
  DATA_SYSCONFIG_WRITE("Enables update of release management data using rest api"),
  DATA_SYSCONFIG_DELETE("Enables deletion of release management data using rest api"),
  
  // demo data for generating "test" tasks/assets/etc 
  DATA_DEMO("Enables manipulation of demo data(generate test tasks/assets in bulk, drop tenants etc...) using rest api"),;
  
  private final String description;
  BuiltInDataPermissions(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description; 
  }
  
}
