package io.resys.thena.storesql;

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.SqlDataMapper;
import io.resys.thena.datasource.TenantTableNames;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlMapperImpl implements SqlDataMapper {

  protected final TenantTableNames ctx;
 
  @Override
  public Tenant repo(Row row) {
    return ImmutableTenant.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .name(row.getString("name"))
        .externalId(row.getString("external_id"))
        .type(StructureType.valueOf(row.getString("type")))
        .prefix(row.getString("prefix"))
        .build();
  }
  
  @Override
  public JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }

  @Override
  public SqlDataMapper withOptions(TenantTableNames options) {
    return new SqlMapperImpl(options);
  }
}
