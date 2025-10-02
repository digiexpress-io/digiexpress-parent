package io.digiexpress.tagomi.spi.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.TagomiStoreConfig;
import io.digiexpress.tagomi.api.TagomiStore.BatchCommand;
import io.digiexpress.tagomi.api.TagomiStore.UpsertBuilder;
import io.digiexpress.tagomi.api.entities.ImmutableTagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.digiexpress.tagomi.spi.support.StoreException;
import io.digiexpress.tagomi.spi.support.StoreException.StoreExceptionMsg;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.git.api.GitCommitActions.CommitResultEnvelope;
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
  public Uni<TagomiContainer> batch(BatchCommand batch) {
    if(batch.getToBeDeleted().isEmpty() && batch.getToBeDeleted().isEmpty() && batch.getToBeCreated().isEmpty()) {
      return Uni.createFrom().item(ImmutableTagomiContainer.builder()
          .tagName(config.getHeadName())
          .build());
    }
    
    final var builder = ImmutableTagomiContainer.builder().tagName(config.getHeadName());
    final List<IsTagomiObject> all = new ArrayList<IsTagomiObject>();
    final var commitBuilder = config.getClient().git(config.getTenantName()).commit().commitBuilder().branchName(config.getHeadName());

    for(final var target : batch.getToBeDeleted()) {
      commitBuilder.remove(target.getId());
      all.add(target);
    }
    for(final var target : batch.getToBeSaved()) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
      QueryBuilderImpl.mapAnyObject(target, builder);
      all.add(target);
    }
    
    for(final var target : batch.getToBeCreated()) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
      QueryBuilderImpl.mapAnyObject(target, builder);
      all.add(target);
    }
    return commitBuilder
        .message("batch" + 
            " created: '" + batch.getToBeCreated().size() + "',"+
            " updated: '" + batch.getToBeSaved().size() + "',"+
            " deleted: '" + batch.getToBeDeleted().size() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return builder.commitAt(commit.getCommit().getDateTime()).commitId(commit.getCommit().getId()).build();
          }
          throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
        });
  }

  @Override
  public <T extends IsTagomiObject> Uni<T> create(T toBeSaved) {
    return config.getClient().git(config.getTenantName()).commit().commitBuilder()
        .branchName(config.getHeadName())
        .message("create type: '" + toBeSaved.getDocType() + "', with id: '" + toBeSaved.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeSaved;
          }
          throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
        });
  }
  
  
  protected StoreExceptionMsg convertMessages(CommitResultEnvelope commit) {
    return StoreExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .args(commit.getMessages().stream().map(message -> message.getText()).collect(Collectors.toList()))
        .build();
  }

}
