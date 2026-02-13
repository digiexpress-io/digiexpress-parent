package io.resys.thena.fs.spi.snapshot;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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



