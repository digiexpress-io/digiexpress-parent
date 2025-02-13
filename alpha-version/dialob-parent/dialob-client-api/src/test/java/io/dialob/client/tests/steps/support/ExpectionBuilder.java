package io.dialob.client.tests.steps.support;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.client.tests.steps.support.StepsBuilder.Expectation;
import io.dialob.client.tests.steps.support.StepsBuilder.WhenMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import java.util.Iterator;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@Slf4j
public class ExpectionBuilder {
  private StepsBuilder stepsBuilder;

  public ExpectionBuilder(StepsBuilder stepsBuilder) {
    this.stepsBuilder = stepsBuilder;
  }

  public ExpectionBuilder expect(String name, Expectation expectConsumer) {
    WhenMessage whenMessage = stepsBuilder.getWhenMessage();
    whenMessage.getExpectations().addExpect(name, expectConsumer);
    return this;
  }

  public ExpectionBuilder accept(String name, Expectation expectConsumer) {
    WhenMessage whenMessage = stepsBuilder.getWhenMessage();
    whenMessage.getExpectations().addAccept(name, expectConsumer);
    return this;
  }

  public ExpectionBuilder expectActions(Consumer<Actions> expectConsumer) {
    return expectActions("expectActions", expectConsumer);
  }

  public ExpectionBuilder expectActions(String name, Consumer<Actions> expectConsumer) {
    return expect(name, actions -> {
      stepsBuilder.getCtx().setRev(actions.getRev());
      expectConsumer.accept(actions);
    });
  }

  public ExpectionBuilder expectActivated() {
    return this;
  }
  public ExpectionBuilder expectPassivation() {
    return this;
  }

  public ExpectionBuilder expectRemoveAll() {
    return expectActions("expectPassivation", actions -> {
      final Iterator<Action> i = actions.getActions().iterator();
      final Action action = i.next();
      assertEquals(Action.Type.RESET, action.getType());
      assertFalse(i.hasNext());
    });
  }

  public ExpectionBuilder expectUpdateWithoutActions() {
    return expectActions("expectUpdateWithoutActions", actions -> {
      if(actions.getActions() == null || actions.getActions().isEmpty()) {
        return;
      }
      log.error("{} do not match with expectUpdateWithoutActions", actions.toString());
      Assertions.fail("We didn't expect any actions, but got some.");
    });
  }

  public StepsBuilder next() {
    return stepsBuilder;
  }

  public ExpectionBuilder finallyAssert(Consumer<String> msg) {
    return this;
  }

  public boolean execute() throws Exception {
    return next().execute();
  }
}
