package io.resys.thena.contract.client.tables;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.ImmutableParty;
import io.resys.thena.contract.client.entities.ImmutablePartyTransitives;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "party",
  order = 1,
  ddl = """
    CREATE TABLE IF NOT EXISTS {party}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      external_id                     VARCHAR(255) NOT NULL,
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      party_type                      VARCHAR(100) NOT NULL,
      party_effective_from            DATE NOT NULL,
      party_effective_to              DATE,

      party_term_start_date           DATE NOT NULL,
      party_term_start_date_interval  INTERVAL NOT NULL,
      party_term_start_date_type      VARCHAR(100) NOT NULL,

      party_term_end_date             DATE,
      party_term_end_date_interval    INTERVAL,
      party_term_end_date_type        VARCHAR(100),

      party_data                      JSONB
    );

    CREATE INDEX IF NOT EXISTS {party}_TYPE_INDEX
      ON {party} (party_type);
    CREATE INDEX IF NOT EXISTS {party}_CONTRACT_INDEX
      ON {party} (contract_id);
    CREATE INDEX IF NOT EXISTS {party}_COMMIT_INDEX
      ON {party} (commit_id);
    CREATE INDEX IF NOT EXISTS {party}_CREATED_COMMIT_INDEX
      ON {party} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {party} ADD CONSTRAINT fk_party_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
  """,
  drop = """
    DROP TABLE {party};
  """
)
public interface PartyTable {

  @TenantSql.FindAll(
    sql = """
      SELECT p.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {party} p
      LEFT JOIN {commit} updated_commit ON p.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON p.created_commit_id = created_commit.commit_id
    """,
    rowMapper = PartyMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT p.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {party} p
      LEFT JOIN {commit} updated_commit ON p.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON p.created_commit_id = created_commit.commit_id
      WHERE p.contract_id = $1
    """,
    rowMapper = PartyMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT party.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {party} party
      LEFT JOIN {commit} updated_commit ON party.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON party.created_commit_id = created_commit.commit_id
      LEFT JOIN {contract} contract ON party.contract_id = contract.id
    """,
    rowMapper = PartyMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT p.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {party} p
      LEFT JOIN {commit} updated_commit ON p.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON p.created_commit_id = created_commit.commit_id
      WHERE p.id = $1
    """,
    rowMapper = PartyMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {party}
      (id, contract_id, external_id, commit_id, created_commit_id,
       party_type, party_effective_from, party_effective_to,
       party_term_start_date, party_term_start_date_interval, party_term_start_date_type,
       party_term_end_date, party_term_end_date_interval, party_term_end_date_type,
       party_data)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
    """,
    propsMapper = PartyInsertMapper.class
  )
  SqlTupleList insertMany(List<Party> parties);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {party}
       SET contract_id = $1, external_id = $2, commit_id = $3,
           party_type = $4, party_effective_from = $5, party_effective_to = $6,
           party_term_start_date = $7, party_term_start_date_interval = $8, party_term_start_date_type = $9,
           party_term_end_date = $10, party_term_end_date_interval = $11, party_term_end_date_type = $12,
           party_data = $13
       WHERE id = $14
    """,
    propsMapper = PartyUpdateMapper.class
  )
  SqlTupleList updateMany(List<Party> parties);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {party} WHERE id = $1",
    propsMapper = PartyDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Party> parties);

  // Mapper classes
  class PartyMapper implements TenantSql.RowMapper<Party> {
    @Override
    public Party apply(Row row) {
      final LocalDate party_effective_to = row.getLocalDate("party_effective_to");
      final LocalDate party_term_end_date = row.getLocalDate("party_term_end_date");
      final var party_term_end_date_interval = TableUtils.toDuration(row, "party_term_end_date_interval");
      final String party_term_end_date_type = row.getString("party_term_end_date_type");
      final JsonObject party_data = row.getJsonObject("party_data");

      return ImmutableParty.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .contractId(TableUtils.toStringUUID(row, "contract_id"))

          .externalId(row.getString("external_id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutablePartyTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .partyType(row.getString("party_type"))
          .partyEffectiveFrom(row.getLocalDate("party_effective_from"))
          .partyEffectiveTo(Optional.ofNullable(party_effective_to))

          // Business dates (expanded)
          .partyTermStartDate(row.getLocalDate("party_term_start_date"))
          .partyTermStartDateInterval(TableUtils.toDuration(row, "party_term_start_date_interval"))
          .partyTermStartDateType(row.getString("party_term_start_date_type"))

          .partyTermEndDate(Optional.ofNullable(party_term_end_date))
          .partyTermEndDateInterval(Optional.ofNullable(party_term_end_date_interval))
          .partyTermEndDateType(Optional.ofNullable(party_term_end_date_type))

          .partyData(Optional.ofNullable(party_data))
          .build();
    }
  }

  class PartyInsertMapper implements TenantSql.PropsMapper<Party> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Party doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        doc.getExternalId(),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getPartyType(),
        doc.getPartyEffectiveFrom(),
        doc.getPartyEffectiveTo().orElse(null),
        doc.getPartyTermStartDate(),
        TableUtils.toInterval(doc.getPartyTermStartDateInterval()),
        doc.getPartyTermStartDateType(),
        doc.getPartyTermEndDate().orElse(null),
        TableUtils.toIntervalOptional(doc.getPartyTermEndDateInterval()),
        doc.getPartyTermEndDateType().orElse(null),
        doc.getPartyData().orElse(null)
      });
    }
  }

  class PartyUpdateMapper implements TenantSql.PropsMapper<Party> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Party doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getContractId()),
        doc.getExternalId(),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getPartyType(),
        doc.getPartyEffectiveFrom(),
        doc.getPartyEffectiveTo().orElse(null),
        doc.getPartyTermStartDate(),
        TableUtils.toInterval(doc.getPartyTermStartDateInterval()),
        doc.getPartyTermStartDateType(),
        doc.getPartyTermEndDate().orElse(null),
        TableUtils.toIntervalOptional(doc.getPartyTermEndDateInterval()),
        doc.getPartyTermEndDateType().orElse(null),
        doc.getPartyData().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }

  class PartyDeleteMapper implements TenantSql.PropsMapper<Party> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Party party) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        TableUtils.toUuid(party.getId())
      });
    }
  }
}
