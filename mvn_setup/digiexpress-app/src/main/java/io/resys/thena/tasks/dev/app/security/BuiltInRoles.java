package io.resys.thena.tasks.dev.app.security;

public enum BuiltInRoles {
  
  LOBBY("Basic access rights, users are added here on first log in"),
  TASK_WORKER("Umbrella role, for accessing tasks, extend from here to narrow down access");
  
  private final String description;
  BuiltInRoles(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description; 
  }
}
