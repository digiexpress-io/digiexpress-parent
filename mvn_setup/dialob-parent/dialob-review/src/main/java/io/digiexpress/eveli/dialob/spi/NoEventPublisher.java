package io.digiexpress.eveli.dialob.spi;

import java.net.InetAddress;

import io.dialob.api.proto.Actions;
import io.dialob.questionnaire.service.api.event.QuestionnaireEventPublisher;

public class NoEventPublisher extends QuestionnaireEventPublisher {

  public NoEventPublisher() {
    super(null, null);

  }

  @Override
  public void opened(String questionnaireId) {
  }

  @Override
  public void created(String questionnaireId) {

  }

  @Override
  public void completed(String tenantId, String questionnaireId) {
  }

  @Override
  public void actions(String questionnaireId, Actions actions) {
  }

  @Override
  public void clientConnected(String questionnaireId, InetAddress client) {
  }

  @Override
  public void clientDisconnected(String questionnaireId, InetAddress client, int closeStatus) {

  }

}
