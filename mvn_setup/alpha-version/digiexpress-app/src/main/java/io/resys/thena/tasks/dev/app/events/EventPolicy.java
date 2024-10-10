package io.resys.thena.tasks.dev.app.events;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;



@ApplicationScoped
public class EventPolicy {
  @Inject EventBus bus;

  public static final String EVENT_AM_UPDATE_STRING = "UI/EVENT_AM_UPDATE";
  
  public Uni<Void> sendAmUpdate() {
    final var body = ImmutableUIEvent.builder().type(UIEventType.EVENT_AM_UPDATE).build();
    bus.<UIEvent>request(UIEventType.EVENT_AM_UPDATE.getAddress(), body).subscribe().with(io.smallrye.mutiny.vertx.UniHelper.NOOP);
    return Uni.createFrom().voidItem();
  }
}
