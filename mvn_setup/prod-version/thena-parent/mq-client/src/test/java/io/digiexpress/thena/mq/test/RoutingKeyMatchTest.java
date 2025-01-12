package io.digiexpress.thena.mq.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.mq.client.api.routing.Router;

public class RoutingKeyMatchTest {

  
  @Test
  public void testSingleWordMatch() {
    Assertions.assertTrue(Router.builder().queueName("task.modify.name").routingKey("*.modify.name").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.name").routingKey("task.*.name").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.name").routingKey("task.modify.*").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.name").routingKey("*.*.*").isMatch());
    
    Assertions.assertFalse(Router.builder().queueName("task.modify.description").routingKey("task").isMatch());    
    Assertions.assertFalse(Router.builder().queueName("task.modify.name").routingKey("*").isMatch());
    Assertions.assertFalse(Router.builder().queueName("task.modify.name").routingKey("*.*").isMatch());
    Assertions.assertFalse(Router.builder().queueName("task.modify.description").routingKey("task.*.name").isMatch());    
    Assertions.assertFalse(Router.builder().queueName("taskcomment.modify.description").routingKey("task.*.name").isMatch());
  }
  
  
  @Test
  public void testWildcardWordMatch() {
    Assertions.assertFalse(Router.builder().queueName("taskcomment.modify.description").routingKey("#.name").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.description").routingKey("#.description").isMatch());
    
    
    Assertions.assertTrue(Router.builder().queueName("task.modify.description").routingKey("#.modify.description").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.description").routingKey("task.#").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.description").routingKey("task.#.description").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.description").routingKey("task.modify.#").isMatch());
    Assertions.assertTrue(Router.builder().queueName("task.modify.description").routingKey("#").isMatch());
    
  }
}
