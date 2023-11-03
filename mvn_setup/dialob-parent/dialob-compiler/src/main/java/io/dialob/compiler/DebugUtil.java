/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.compiler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import io.dialob.executor.command.Command;
import io.dialob.executor.command.UpdateCommand;
import io.dialob.executor.command.event.Event;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class DebugUtil {

  private static String stripImmutablePrefix(String name) {
    if (name.startsWith("Immutable")) {
      return name.substring(9);
    }
    return name;
  }

  public static String commandToString(Command<?> command) {
    final String commandName = stripImmutablePrefix(command.getClass().getSimpleName());
    if (command instanceof UpdateCommand) {
      UpdateCommand updateCommand = (UpdateCommand) command;
      return commandName + "(" + IdUtils.toString(updateCommand.getTargetId()) + ")";
    }
    return commandName;
  }

  // TODO move away from here
  public static void dumpDotFile(final Map<ItemId, List<Command<?>>> itemCommands) {
    Set<Pair<ItemId, Event>> events = itemCommands.entrySet().stream()
      .flatMap(commandEntry -> commandEntry.getValue().stream()
        .flatMap(command -> command.getTriggers().stream())
        .flatMap(trigger -> trigger.getAllEvents().stream())
        .map(event -> Pair.of(commandEntry.getKey(), event))).collect(toSet());

    Map<Pair<ItemId, Event>,List<UpdateCommand<?,?>>> eventToCommands = new HashMap<>();
    Map<UpdateCommand<?,?>,List<Pair<ItemId, Event>>> commandToEvent = new HashMap<>();

    itemCommands.values().stream()
      .flatMap(List::stream)
      .filter(c -> c instanceof UpdateCommand)
      .map(c -> (UpdateCommand<?,?>) c)
      .forEach(command ->
        events.stream().filter(event -> command.getEventMatchers().stream().anyMatch(eventMatcher -> eventMatcher.matches(event.getRight())))
          .forEach(event -> eventToCommands.computeIfAbsent(event, event1 -> new ArrayList<>()).add(command)));

    itemCommands.values().stream()
      .flatMap(List::stream)
      .filter(c -> c instanceof UpdateCommand)
      .map(c -> (UpdateCommand<?,?>) c)
      .forEach(command ->
        command.getTriggers()
          .forEach(trigger ->
            commandToEvent.computeIfAbsent(command, command1 -> new ArrayList<>())
            .addAll(trigger.getAllEvents().stream().map(e -> Pair.of((ItemId) command.getTargetId(), e)).collect(toList()))));

    try(OutputStreamWriter writer = new FileWriter("deps.dot")) {
      SortedSet<String> nodes = new TreeSet<>();
      SortedSet<String> edges = new TreeSet<>();
      writer.write("strict digraph {\n");
      for (Map.Entry<Pair<ItemId, Event>,List<UpdateCommand<?,?>>> entry : eventToCommands.entrySet()) {
        for (UpdateCommand<?,?> command : entry.getValue()) {

          nodes.add(DebugUtil.commandToString(command) + "\" [shape=rect]\n");

          edges.add(eventName(entry.getKey()) + "\" -> \"" + DebugUtil.commandToString(command) + "\"\n");
        }
      }
      for (Map.Entry<UpdateCommand<?,?>,List<Pair<ItemId, Event>>> entry : commandToEvent.entrySet()) {

        nodes.add(DebugUtil.commandToString(entry.getKey()) + "\" [shape=rect]\n");
        for (Pair<ItemId, Event> event : entry.getValue()) {

          edges.add(DebugUtil.commandToString(entry.getKey()) + "\" -> \"" + eventName(event) + "\"\n");
        }
      }

      nodes.forEach(s -> {
        try {
          writer.write("  \"" + s);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
      edges.forEach(s -> {
        try {
          writer.write("  \"" + s);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
      writer.write("}\n");
    } catch (IOException e) {
      LOGGER.error("dot dump failed.", e);
    }
  }

  private static String eventName(Pair<ItemId, Event> event) {
    return event.getRight().getClass().getSimpleName().substring(9) + "(" + IdUtils.toString(event.getLeft()) + ")";
  }

}
