package io.resys.thena.docdb.file.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.file.FileBuilder.TagFileBuilder;
import io.resys.thena.docdb.file.tables.ImmutableFileStatement;
import io.resys.thena.docdb.file.tables.ImmutableFileTuple;
import io.resys.thena.docdb.file.tables.ImmutableTagTableRow;
import io.resys.thena.docdb.file.tables.Table;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.spi.ClientCollections;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagFileBuilderImpl implements TagFileBuilder {
  
  private final ClientCollections ctx;
  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create TAG table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).getTags().create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  @Override
  public FileStatement constraints() {
    return ImmutableFileStatement.builder()
        .value("Apply constraints on table TAG")
        .command(conn -> Arrays.asList(new Table.Row() {}))
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("Select all from TAG table")
        .command((root) -> root.getRepoTable(ctx).getTags().getRows())
        .build();
  }
  @Override
  public FileTuple getByName(String name) {
    return ImmutableFileTuple.builder()
        .value("Select by name from TAG table")
        .command((root) -> root.getRepoTable(ctx).getTags().getRows()
            .stream().filter(r -> r.getId().equals(name)).collect(Collectors.toList()))
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileStatement getFirst() {
    return ImmutableFileStatement.builder()
        .value("Select first from TAG table")
        .command((root) -> {
          final var refs = root.getRepoTable(ctx).getTags().getRows();
          if(refs.isEmpty()) {
            return Collections.emptyList();
          }
          return Arrays.asList(refs.get(0));
        })
        .build();
  }
  @Override
  public FileTuple deleteByName(String name) {
    return ImmutableFileTuple.builder()
        .value("Delete row from TAG table")
        .command((root) -> {
          
          final var exists = root.getRepoTable(ctx).getTags().getRows()
            .stream().filter(r -> r.getId().equals(name))
            .findFirst();
          if(exists.isEmpty()) {
            throw new IllegalArgumentException("Can't find TAG with name: " + name);
          }
          
          root.getRepoTable(ctx).getTags().delete(exists.get());
          
          return Arrays.asList(exists.get());
        })
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileTuple insertOne(Tag newTag) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into TAG table")
        .command((root) -> {
          
          final var exists = root.getRepoTable(ctx).getTags().getRows()
            .stream().filter(r -> r.getId().equals(newTag.getName()))
            .findFirst();
          if(!exists.isEmpty()) {
            return Arrays.asList(exists.get());
          }
          
          final var newRow = ImmutableTagTableRow.builder()
              .id(newTag.getName()) 
              .commit(newTag.getCommit()) 
              .datetime(newTag.getDateTime().toString()) 
              .author(newTag.getAuthor())
              .message(newTag.getMessage())
              .build();
          root.getRepoTable(ctx).getTags().insert(newRow);
          
          return Arrays.asList(newRow);
        })
        .props(Tuple.of(newTag.getName(), newTag.getCommit(), newTag.getDateTime().toString(), newTag.getAuthor(), newTag.getMessage()))
        .build();
  }
}
