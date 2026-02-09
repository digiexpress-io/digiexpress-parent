package io.resys.thena.fs.spi.snapshot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.NewFile;
import io.resys.thena.fs.api.commits.CommitBuilder.NewFolder;
import io.resys.thena.fs.entities.Node;


public interface ChangeCommand {

  public static record RmCommand(String docId) implements ChangeCommand {}

  public static record NewFolderCommand(Consumer<NewFolder> consumer) implements ChangeCommand {}
  public static record NewFileCommand(Consumer<NewFile> consumer) implements ChangeCommand {}

  public static record MergeFolderCommand(String docId, BiConsumer<Node, MergeFolder> consumer) implements ChangeCommand {}
  public static record MergeFileCommand(String docId, BiConsumer<Node, MergeFile> consumer) implements ChangeCommand {}  
}



