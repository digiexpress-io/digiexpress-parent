package io.digiexpress.tagomi.api;

import java.util.List;
import java.util.Optional;

import io.digiexpress.tagomi.api.commands.TagomiDeleteCommands;
import io.digiexpress.tagomi.api.commands.TagomiCreateCommands;
import io.digiexpress.tagomi.api.commands.TagomiUpdateCommands;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.smallrye.mutiny.Uni;



public interface TagomiComposer {
  TagomiCreateCommands create();
  TagomiUpdateCommands update();
  TagomiDeleteCommands delete();
  TagomiQueryBuilder query();
  

  interface TagomiQueryBuilder {
    Uni<Optional<io.resys.thena.api.entities.git.Branch>> findCurrentBranch();
    Uni<TagomiContainer> getCurrentHead();
    Uni<List<IsTagomiObject>> findAllCurrentHeadObjects(List<String> ids, TagomiContainer.TagomiDocType type);
    Uni<TagomiContainer> getOneRelease(String releaseId);
  }
}
