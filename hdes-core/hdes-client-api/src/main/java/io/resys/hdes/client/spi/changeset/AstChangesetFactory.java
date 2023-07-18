package io.resys.hdes.client.spi.changeset;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Range;

import io.resys.hdes.client.api.ast.AstChangeset;
import io.resys.hdes.client.api.ast.AstChangeset.CommandsAndChanges;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableCommandsAndChanges;
import io.resys.hdes.client.spi.changeset.beans.AstChangesetBean;
import io.resys.hdes.client.spi.util.HdesAssert;

public class AstChangesetFactory {

  public static SourceBuilder src() {
    return new SourceBuilder();
  }

  public static CommandsAndChanges src(List<AstCommand> src, Integer rev) {
    return new SourceBuilderFromCommands(src, rev).build();
  }

  private static class SourceBuilderFromCommands {
    private final List<AstCommand> src; 
    private final Integer rev;
    private final List<AstCommand> commands = new ArrayList<>();
    private final SourceBuilder sourceBuilder = new SourceBuilder();
    
    public SourceBuilderFromCommands(List<AstCommand> src, Integer rev) {
      this.src = src;
      this.rev = rev;
    }
    
    public CommandsAndChanges build() {
      if (rev != null) {
        int limit = rev;
        int runningVersion = 0;
        for (AstCommand command : src) {
          if (runningVersion++ > limit) {
            break;
          }
          visitCommand(command);
        }
      } else {
        src.forEach(command -> visitCommand(command));
      }
      return ImmutableCommandsAndChanges.builder().commands(commands).src(sourceBuilder.build()).build();
    }
    
    private void visitCommand(AstCommand command) {
      
      AstCommandValue type = command.getType();

      String text = command.getValue();
      if (type == AstCommandValue.DELETE) {
        int line = Integer.parseInt(command.getId());
        sourceBuilder.delete(line, Integer.parseInt(command.getValue()));
      } else if (type == AstCommandValue.ADD) {
        int line = Integer.parseInt(command.getId());
        sourceBuilder.add(line, text);
      } else if(type == AstCommandValue.SET_BODY) {
        String lines[] = text.split("\\r?\\n");
        int lineNumber = 0;
        for(final var lineValue : lines) {
          sourceBuilder.add(lineNumber++, lineValue);  
        }
        
      } else {
        int line = Integer.parseInt(command.getId());
        sourceBuilder.set(line, text);
      }
      commands.add(command);
    }
  }

  public static class SourceBuilder {
    private final List<AstChangesetBean> sourcesAdded = new ArrayList<>();
    private final List<AstChangesetBean> sourcesDeleted = new ArrayList<>();

    public SourceBuilder add(int line, String value) {
      sourcesAdded.stream().filter(s -> s.getLine() >= line).forEach(s -> s.setLine(s.getLine() + 1));
      sourcesAdded.add(new AstChangesetBean(line,
          ImmutableAstCommand.builder().id(String.valueOf(line)).value(value).type(AstCommandValue.ADD).build()));
      return this;
    }

    public SourceBuilder set(int line, String value) {
      Optional<AstChangesetBean> source = sourcesAdded.stream().filter(s -> s.getLine() == line).findFirst();
      HdesAssert.isTrue(source.isPresent(), () -> String.format("Can't change value of non existing line: %s!", line));
      source.get().add(
          ImmutableAstCommand.builder().id(String.valueOf(line)).value(value).type(AstCommandValue.SET).build());
      return this;
    }

    public SourceBuilder delete(int line) {
      Iterator<AstChangesetBean> sources = sourcesAdded.iterator();
      while (sources.hasNext()) {
        AstChangesetBean source = sources.next();
        if (source.getLine() == line) {
          sources.remove();
          sourcesDeleted.add(source);
        } else if (source.getLine() > line) {
          source.setLine(source.getLine() - 1);
        }
      }
      return this;
    }

    public SourceBuilder delete(int from, int to) {
      Range<Integer> range = Range.closed(from, to);
      int linesToDelete = (to - from) + 1;

      Iterator<AstChangesetBean> sources = sourcesAdded.iterator();
      while (sources.hasNext()) {
        AstChangesetBean source = sources.next();

        if (range.contains(source.getLine())) {
          sources.remove();
          sourcesDeleted.add(source);
        } else if (source.getLine() > to) {
          source.setLine(source.getLine() - linesToDelete);
        }
      }

      return this;
    }

    public List<AstChangeset> build() {
      Collections.sort(sourcesAdded);
      return new ArrayList<>(sourcesAdded);
    }
  }
}
