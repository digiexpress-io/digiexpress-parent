package io.dialob.client.spi;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ImmutableAction;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.client.api.DialobErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class DialobErrorHandlerImpl implements DialobErrorHandler {
  private final boolean reportStackTrace;

  @Override
  public Actions toActions(Exception e) {
    final ImmutableActions.Builder actions = ImmutableActions.builder();
    if(e instanceof DocumentNotFoundException) {

      final var dex = (DocumentNotFoundException) e;
      log.debug("Action QUESTIONNAIRE_NOT_FOUND: backend response '{}'", e.getMessage());
      actions.addActions(ImmutableAction.builder()
          .type(Action.Type.SERVER_ERROR)
          .serverEvent(true)
          .message("not found")
          .id(dex.getId()).build());
    } else {
      log.debug("Error in websocket handler", e);
      actions.actions(Collections.singletonList(createNotifyServerErrorAction(e)));
    }

    return actions.build();
  }


  private Action createNotifyServerErrorAction(Exception e) {
    final ImmutableAction.Builder action = ImmutableAction.builder()
      .type(Action.Type.SERVER_ERROR)
      .serverEvent(true);
    if (reportStackTrace) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      action.message(e.getMessage());
      action.trace(sw.toString());
    }
    return action.build();
  }
}
