package io.digiexpress.thena.batch.client.api;

public class BatchException extends RuntimeException {
  private static final long serialVersionUID = 4311634600357697485L;

  public BatchException(String msg) {
    super(msg);
  }
}
