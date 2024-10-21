package io.digiexpress.eveli.client.api;


public interface PortalClient {
  AttachmentCommands attachments();
  DialobCommands dialob();
  TaskCommands task();
  NotificationCommands notification();
  WorkflowCommands workflow();
  ProcessCommands process();
  ProcessAuthorizationCommands processAuthorization();
  HdesCommands hdes();
  AssetReleaseCommands assetRelease();
}
