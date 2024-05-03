package io.resys.thena.tasks.dev.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.vertx.ConsumeEvent;
import io.resys.thena.tasks.dev.app.events.EventPolicy;
import io.resys.thena.tasks.dev.app.events.UIEvent;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/q/digiexpress/api/events")
public class UiEventsResource {

  private final Map<String, Session> sessions = new ConcurrentHashMap<>(); 
  @Inject CurrentUser currentUser;
  
  

  @OnOpen
  public void onOpen(Session session) {
    final var username = currentUser.getUserId();
    broadcast("User " + username + " joined");
    sessions.put(username, session);
  }

  @OnClose
  public void onClose(Session session) {
    final var username = currentUser.getUserId();
    sessions.remove(username);
    broadcast("User " + username + " left");
  }
  
  @ConsumeEvent(EventPolicy.EVENT_AM_UPDATE_STRING)
  public void publishAmUpdate(io.vertx.mutiny.core.eventbus.Message<UIEvent> msg) {
     broadcast("Hello " + msg.body());
     msg.reply(msg.body());
  }
  
  private void broadcast(String message) {
    sessions.values().forEach(s -> {
      s.getAsyncRemote().sendObject(message, result ->  {
          if (result.getException() != null) {
              System.out.println("Unable to send message: " + result.getException());
          }
      });
    });
  }
}
