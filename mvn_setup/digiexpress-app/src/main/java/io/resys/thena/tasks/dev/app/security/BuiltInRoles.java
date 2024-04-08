package io.resys.thena.tasks.dev.app.security;

public enum BuiltInRoles {
  
  LOBBY("Basic access rights, users are added here on first log in");
  
  private final String description;
  BuiltInRoles(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description; 
  }
  
}
