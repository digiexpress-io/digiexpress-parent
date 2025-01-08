package io.digiexpress.thena.mq.client.spi.routing;

import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.resys.thena.support.RepoAssert;

public class RoutingKeyMatcher {

  public boolean isMatch(String routingKey, Queue queue) {
    RepoAssert.notEmpty(routingKey, () -> "routing key can't be empty!");
    RepoAssert.notNull(queue, () -> "queue can't be null!");
  }
}
