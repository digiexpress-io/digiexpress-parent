package io.resys.thena.tasks.dev.app.events;

public enum UIEventType {
  
  EVENT_AM_UPDATE(EventPolicy.EVENT_AM_UPDATE_STRING);
  
  private final String address;
  
  public String getAddress() {
    return address;
  }

  private UIEventType(String address) {
    this.address = address;
  }
}