package io.resys.thena.tasks.dev.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.vertx.ConsumeEvent;
import io.resys.thena.tasks.dev.app.events.EventPolicy;
import io.resys.thena.tasks.dev.app.events.UIEvent;
import io.resys.thena.tasks.dev.app.events.UIEventEncoder;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ServerEndpoint(value = "/q/digiexpress/api/events", 
  encoders = {UIEventEncoder.class},
  decoders = {}
)
public class UiEventsResource {

  private final Map<String, Session> sessions = new ConcurrentHashMap<>(); 
  @Inject CurrentUser currentUser;
  
  @OnOpen
  public void onOpen(Session session) {
    final var username = currentUser.getUserId();
    log.debug("User: {} has joined", username);
    sessions.put(username, session);
  }

  @OnClose
  public void onClose(Session session) {
    final var username = currentUser.getUserId();
    sessions.remove(username);
    log.debug("User: {} has left", username);
  }
  
  @ConsumeEvent(EventPolicy.EVENT_AM_UPDATE_STRING)
  public void publishAmUpdate(io.vertx.mutiny.core.eventbus.Message<UIEvent> msg) {
     broadcast(msg.body());
     msg.reply(msg.body());
  }
  
  private void broadcast(UIEvent message) {
    sessions.values().forEach(s -> {
      s.getAsyncRemote().sendObject(message, result ->  {
          if (result.getException() != null) {
            log.error("Unable to send message: {}", result.getException().getMessage(), result.getException());
          }
      });
    });
  }
}
