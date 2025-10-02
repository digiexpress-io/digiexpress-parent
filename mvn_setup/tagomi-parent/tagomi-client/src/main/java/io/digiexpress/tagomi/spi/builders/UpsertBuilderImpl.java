package io.digiexpress.tagomi.spi.builders;

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.TagomiStore.BatchCommand;
import io.digiexpress.tagomi.api.TagomiStore.UpsertBuilder;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.digiexpress.tagomi.spi.TagomiStoreConfig;
import io.digiexpress.tagomi.spi.support.StoreException;
import io.digiexpress.tagomi.spi.support.StoreException.StoreExceptionMsg;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.git.api.GitCommitActions.CommitResultEnvelope;
import io.resys.thena.git.api.GitPullActions;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class UpsertBuilderImpl implements UpsertBuilder {
  private final TagomiStoreConfig config;
  
  @Override
  public <T extends IsTagomiObject> Uni<T> delete(T toBeDeleted) {
    return config.getClient().git(config.getTenantName()).commit().commitBuilder()
        .branchName(config.getHeadName())
        .message("Delete type: '" + toBeDeleted.getDocType() + "', with id: '" + toBeDeleted.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .remove(toBeDeleted.getId())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeDeleted;
          }
          // TODO
          throw new StoreException("DELETE_FAIL", toBeDeleted, convertMessages(commit));
        });
  }

  @Override
  public <T extends IsTagomiObject> Uni<T> save(T toBeSaved) {
    return config.getClient().git(config.getTenantName()).commit().commitBuilder()
        .branchName(config.getHeadName())
        .message("Save type: '" + toBeSaved.getDocType() + "', with id: '" + toBeSaved.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeSaved;
          }
          // TODO
          throw new StoreException("SAVE_FAIL", toBeSaved, convertMessages(commit));
        });
  }

  @Override
  public Uni<List<? extends IsTagomiObject>> saveAll(List<IsTagomiObject> toBeSaved) {
    final var commitBuilder = config.getClient().git(config.getTenantName()).commit().commitBuilder().branchName(config.getHeadName());
    
    for(final var target : toBeSaved) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
    }
    
    return commitBuilder
        .message("Batch operation, saving all: " + toBeSaved.size())
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeSaved;
          }
          // TODO
          throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
        });
  }

  @Override
  public <T extends IsTagomiObject> Uni<T> get(String blobId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<TagomiContainer> batch(BatchCommand batch) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IsTagomiObject> Uni<T> create(T toBeSaved) {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  
  protected StoreExceptionMsg convertMessages(CommitResultEnvelope commit) {
    return StoreExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .args(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  protected StoreExceptionMsg convertMessages1(QueryEnvelope<GitPullActions.PullObject> state) {
    return StoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .args(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }
  protected StoreExceptionMsg convertMessages2(QueryEnvelope<GitPullActions.PullObjects> state) {
    return StoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .args(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }


}
