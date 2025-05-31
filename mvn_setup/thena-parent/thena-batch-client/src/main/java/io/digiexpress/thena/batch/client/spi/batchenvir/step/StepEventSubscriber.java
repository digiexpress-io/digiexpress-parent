package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.util.concurrent.Flow.Subscription;

import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class StepEventSubscriber implements MultiSubscriber<StepEvent> {
  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;
  
  Subscription subscription;
  
  @Override
  public void onSubscribe(Subscription subscription) {
    // TODO Auto-generated method stub
    System.out.println("FFFF");
    this.subscription = subscription;
    subscription.request(Long.MAX_VALUE); // Request the first item
  }

  @Override
  public void onItem(StepEvent item) {
    // TODO Auto-generated method stub
    

    System.out.println("XXXXXXX ======================== " + item);
    //subscription.request(1);
  }

  @Override
  public void onFailure(Throwable failure) {
    // TODO Auto-generated method stub
    System.out.println("FFFF");failure.printStackTrace();
  }

  @Override
  public void onCompletion() {
    // TODO Auto-generated method stub
    System.out.println("FFFF");
  }

}
