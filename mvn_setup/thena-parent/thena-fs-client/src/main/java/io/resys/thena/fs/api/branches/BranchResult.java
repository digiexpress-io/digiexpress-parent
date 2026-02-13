package io.resys.thena.fs.api.branches;

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.fs.entities.Ref;
import jakarta.annotation.Nullable;

/**
 * Result envelope containing the outcome of a branch operation.
 * Includes the created/updated branch reference and operation status.
 */
@Value.Immutable
public
interface BranchResult extends ThenaEnvelope {
  /**
   * @return the tenant identifier where the branch operation was performed
   */
  String getTenantId();
  
  /**
   * @return the created/updated branch reference, null if operation failed
   */
  @Nullable Ref getBranch();
  
  /**
   * @return the overall status of the branch operation
   */
  CommitResultStatus getStatus();
  
  /**
   * @return list of diagnostic messages from the branch operation
   */
  List<Message> getMessages();
}