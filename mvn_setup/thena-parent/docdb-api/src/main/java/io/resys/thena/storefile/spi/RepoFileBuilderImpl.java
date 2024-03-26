package io.resys.thena.storefile.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storefile.FileBuilder.RepoFileBuilder;
import io.resys.thena.storefile.tables.ImmutableFileStatement;
import io.resys.thena.storefile.tables.ImmutableFileTuple;
import io.resys.thena.storefile.tables.ImmutableRepoTableRow;
import io.resys.thena.storefile.tables.Table;
import io.resys.thena.storefile.tables.RepoTable.RepoTableRow;
import io.resys.thena.storefile.tables.Table.FileStatement;
import io.resys.thena.storefile.tables.Table.FileTuple;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepoFileBuilderImpl implements RepoFileBuilder {
  private final DbCollections ctx;

  @Override
  public FileTuple exists() {
    return ImmutableFileTuple.builder()
        .value("Does REPO table exist")
        .command((conn) -> {
          return Arrays.asList(new Table.RowExists() {
            public boolean getExists() {
              return conn.getRepoTable(ctx).getExists();
            }
          });          
        })
        .props(Tuple.of("repo"))
        .build();
  }  
  @Override
  public FileStatement create() {
    return ImmutableFileStatement.builder()
        .value("create REPO table if it does not exist")
        .command((root) -> {
          root.getRepoTable(ctx).create();
          return Arrays.asList(new Table.Row() {});
        })
        .build();
  }
  
  @Override
  public FileStatement findAll() {
    return ImmutableFileStatement.builder()
        .value("select all from REPO table")
        .command((root) -> 
          new ArrayList<>(root.getRepoTable(ctx).getRows())
        )
        .build();
  }
  @Override
  public FileTuple getByName(String name) {
    return ImmutableFileTuple.builder()
        .value("select from REPO table by name")
        .command((root) -> 
          root.getRepoTable(ctx)
          .getRows().stream().filter((RepoTableRow r) -> r.getName().equals(name))
          .collect(Collectors.toList())
        )
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileTuple getByNameOrId(String name) {
    return ImmutableFileTuple.builder()
        .value("select from REPO table by name or id")
        .command((root) -> {
          return root.getRepoTable(ctx)
            .getRows().stream().filter((RepoTableRow r) -> r.getName().equals(name) || r.getId().equals(name))
            .collect(Collectors.toList());
        })
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public FileTuple insertOne(Tenant newRepo) {
    return ImmutableFileTuple.builder()
        .value("Insert new row into REPO table")
        .command((root) -> {
          
          return Arrays.asList(root.getRepoTable(ctx).insert(ImmutableRepoTableRow.builder()
              .id(newRepo.getId())
              .rev(newRepo.getRev())
              .prefix(newRepo.getPrefix())
              .name(newRepo.getName())
              .type(newRepo.getType())
              .build())
              );
        })
        .props(Tuple.of(newRepo.getId(), newRepo.getRev(), newRepo.getPrefix(), newRepo.getName(), newRepo.getType()))
        .build();
  }
}
