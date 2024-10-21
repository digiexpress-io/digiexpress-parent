package io.digiexpress.eveli.client.event.runnables;

import io.digiexpress.eveli.client.api.PortalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QuestionnaireCompletionRunnable  implements Runnable {

  private final PortalClient client;
  private final String questionnaireId;

  @Override
  public void run() {
    log.info("Runnable Task with questionnaire id {} started", questionnaireId);
    client.hdes().execute(questionnaireId);
    log.info("Runnable Task with questionnaire id {} completed", questionnaireId);
  }
}
