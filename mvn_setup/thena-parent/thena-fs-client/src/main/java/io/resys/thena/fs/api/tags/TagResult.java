package io.resys.thena.fs.api.tags;

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.fs.entities.Tag;
import jakarta.annotation.Nullable;

/**
 * Result envelope containing the outcome of a commit operation.
 * Includes the created commit, operation status, and any diagnostic messages.
 */
@Value.Immutable
public
interface TagResult extends ThenaEnvelope {
  /**
   * @return the tenant identifier where the tag was applied
   */
  String getTenantId();
  
  /**
   * @return the created Tag object, null if tagging failed
   */
  @Nullable Tag getTag();
  
  /**
   * @return the overall status of the tag operation
   */
  CommitResultStatus getStatus();
  
  /**
   * @return list of diagnostic messages from the tag process
   */
  List<Message> getMessages();
}