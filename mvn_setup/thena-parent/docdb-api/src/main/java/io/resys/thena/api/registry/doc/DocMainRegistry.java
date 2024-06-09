package io.resys.thena.api.registry.doc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import io.vertx.core.json.JsonObject;


public interface DocMainRegistry extends ThenaRegistryService<Doc, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple findAll(DocFilter filter);
  
  ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  ThenaSqlClient.SqlTuple deleteMany(List<String> id);
  ThenaSqlClient.Sql findAll();
  
  ThenaSqlClient.SqlTupleList insertMany(List<Doc> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<Doc> docs);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Doc> defaultMapper();
  

  @Value.Immutable
  interface DocFlatted {
    String getExternalId();
    
    String getDocId();
    String getDocType();
    String getDocCreatedWithCommitId();
    Optional<String> getDocParentId();
    Doc.DocStatus getDocStatus();
    Optional<JsonObject> getDocMeta();


    String getBranchId();
    String getBranchName();
    String getBranchCreatedWithCommitId();
    
    Doc.DocStatus getBranchStatus();
    JsonObject getBranchValue();
    
    String getCommitAuthor();
    LocalDateTime getCommitDateTime();
    String getCommitMessage();
    Optional<String> getCommitParent();
    String getCommitId();
    
    Optional<String> getDocLogId();
    Optional<JsonObject> getDocLogValue();
  }
  
}