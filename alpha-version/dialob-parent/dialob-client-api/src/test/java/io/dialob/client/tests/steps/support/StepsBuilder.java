package io.dialob.client.tests.steps.support;

import io.dialob.api.proto.*;
import io.dialob.client.tests.client.DialobClientTestImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class StepsBuilder {
  private final Context ctx;
  private WhenMessage whenMessage;


  public interface Expectation {
    void accept(Actions message) throws Exception;
  }
  public interface Expectations extends Expectation {
    boolean isCompleted();
    boolean isSuccess();
    void addExpect(String name, Expectation expectConsumer);
    void addAccept(String name, Expectation expectConsumer);
  }


  public ExpectionBuilder when(WhenMessage whenMessage) {
    this.whenMessage = whenMessage;
    this.ctx.addStep(this.whenMessage);
    this.whenMessage.setExpectations(new OutOfOrderExpectations());
    return new ExpectionBuilder(this);
  }

  public WhenMessage getWhenMessage() {
    return whenMessage;
  }

  public ExpectionBuilder when(String name, Actions actions) {
    return when(new WhenMessage(name) {
      @Override
      public void accept() throws Exception {
        final var next = ctx.answerSession(actions);
        this.getExpectations().accept(next);
      }
    });
  }

  public ExpectionBuilder when(String name, Action action) {
    return when(name, ImmutableActions.builder()
      .addActions(action).build());
  }

  public ExpectionBuilder answerQuestion(String questionId, String answer) {
    return when("answerQuestion(\"" + questionId + "\",\"" + answer + "\")", ImmutableAction.builder()
      .type(Action.Type.ANSWER)
      .answer(answer)
      .id(questionId).build());
  }


  public ExpectionBuilder setLocale(String locale) {
    return when("setLocale(\"" + locale + "\")", ActionsFactory.setLocale(locale));
  }


  public ExpectionBuilder addRow(String rowGroupId) {
    return when("addRow(\"" + rowGroupId + "\")", ImmutableAction.builder()
      .type(Action.Type.ADD_ROW)
      .id(rowGroupId).build());
  }

  public ExpectionBuilder deleteRow(String rowGroupId) {
    return when("deleteRow(\"" + rowGroupId + "\")", ImmutableAction.builder()
      .type(Action.Type.DELETE_ROW)
      .id(rowGroupId).build());
  }

  public ExpectionBuilder nextPage() {
    final Action action = ImmutableAction.builder()
      .type(Action.Type.NEXT).build();
    return when("nextPage()", action);
  }

  public ExpectionBuilder prevPage() {
    return when("prevPage()", ImmutableAction.builder()
      .type(Action.Type.PREVIOUS).build());
  }

  public boolean execute() throws Exception {
    return ctx.execute();
  }

  @Slf4j
  private static class OutOfOrderExpectations implements Expectations {

    private List<Expectation> expectations = new ArrayList<>();

    private List<Expectation> accepted = new ArrayList<>();

    private List<String> unexpected = new ArrayList<>();

    @Override
    public void accept(Actions message) throws Exception {
      Iterator<Expectation> i = expectations.iterator();
      List<AssertionError> assertionErrors = new ArrayList<>();
      while(i.hasNext()) {
        final Expectation expectation = i.next();
        try {
          expectation.accept(message);
          i.remove();
          return;
        } catch (AssertionError error) {
          assertionErrors.add(error);
        } catch (Throwable t) {
        }
      }
      i = accepted.iterator();
      while(i.hasNext()) {
        final Expectation expectation = i.next();
        try {
          expectation.accept(message);
          i.remove(); // Just once or multiple??
          return;
        } catch (AssertionError error) {
          assertionErrors.add(error);
        } catch (Throwable t) {
        }
      }
      if (!assertionErrors.isEmpty()) {
        log.error("Assertions failed\n{}", assertionErrors);
      }
      unexpected.add(DialobClientTestImpl.Builder.MAPPER.writeValueAsString(message));
    }

    @Override
    public boolean isCompleted() {
      return expectations.isEmpty() || !unexpected.isEmpty();
    }

    @Override
    public boolean isSuccess() {
      return expectations.isEmpty() && unexpected.isEmpty();
    }

    @Override
    public void addExpect(String name, Expectation expectation) {
      if (name != null) {
        expectation = new NamedExpectation(name, expectation);
      }
      expectations.add(expectation);
    }

    @Override
    public void addAccept(String name, Expectation expectConsumer) {
      accepted.add(expectConsumer);
    }

    @Override
    public String toString() {
      return "Unexpected messages: " + unexpected + "\n" +
        "Unsatisfied expectations: " + expectations;
    }
  }

  public Context getCtx() {
    return ctx;
  }



  public static abstract class WhenMessage {

    private final String name;


    private Expectations expectations;

    protected WhenMessage(String name) {
      this.name = name;
    }

    public abstract void accept() throws Exception;

    public String getName() {
      return name;
    }

    public Expectations getExpectations() {
      return expectations;
    }

    public void setExpectations(Expectations expectations) {
      this.expectations = expectations;
    }
  }


  public static class NamedExpectation implements Expectation {

    private final String name;
    private final Expectation delegate;

    public NamedExpectation(String name, Expectation delegate) {
      this.name = name;
      this.delegate = delegate;
    }

    @Override
    public void accept(Actions message) throws Exception {
      delegate.accept(message);
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
