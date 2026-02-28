package io.resys.limaone.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

public class UniTests {

  
  @Test
  public void test() {

    final var xx = Uni.createFrom().item(() -> {
      return find();
    }).await().atMost(Duration.ofMillis(1000));
    
    System.out.println(xx);
  }
  
  
  public String find() {
    return Uni.createFrom().item(() -> "Hello")
    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
    .await().atMost(Duration.ofMillis(1000));
  }
}
