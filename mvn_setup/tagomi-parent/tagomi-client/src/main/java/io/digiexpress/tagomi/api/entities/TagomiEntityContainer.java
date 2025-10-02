package io.digiexpress.tagomi.api.entities;

import org.immutables.value.Value;

import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.resys.thena.git.api.GitPullActions;

@Value.Immutable
public interface TagomiEntityContainer {
  GitPullActions.PullObject getSrc();
  IsTagomiObject getEntity();
}
