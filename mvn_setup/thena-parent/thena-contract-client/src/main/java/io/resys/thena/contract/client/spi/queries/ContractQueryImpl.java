package io.resys.thena.contract.client.spi.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ContractQueryActions.ContractQuery;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.entities.ContractDocType;
import io.resys.thena.contract.client.tables.ContractDb;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContractQueryImpl implements ContractQuery {

  private final Uni<ContractDb> state;
  
  private final List<String> contractIds = new ArrayList<>();
  private final List<ContractDocType> excludedDocs = new ArrayList<>();
  private boolean lockForUpdate;

  @Override
  public ContractQuery lockForUpdate() {
    this.lockForUpdate = true;
    return this;
  }

  @Override
  public ContractQuery excludeDocs(ContractDocType... docs) {
    this.excludedDocs.addAll(Arrays.asList(docs));
    return this;
  }

  @Override
  public ContractQuery addContractId(String id) {
    this.contractIds.add(id);
    return this;
  }

  @Override
  public ContractQuery addAllContractId(List<String> ids) {
    this.contractIds.addAll(ids);
    return this;
  }

  private Multi<ContractContainer> startQuery(ContractDb state) {
    final var query = state.contracts();
    
    if (this.contractIds != null && !this.contractIds.isEmpty()) {
      query.contractId(this.contractIds.toArray(new String[]{}));
    }
    
    if (this.excludedDocs != null && !this.excludedDocs.isEmpty()) {
      query.excludeDocs(this.excludedDocs.toArray(new ContractDocType[]{}));
    }
    
    if (this.lockForUpdate) {
      query.lockForUpdate();
    }
    
    state.query().queryContract().findAllByContractId(null)
    
    return query;
  }

  @Override
  public Uni<QueryEnvelope<ContractContainer>> get(String contractIdOrExtId) {
    return this.state
        .onItem().transformToUni(state -> {
          return startQuery(state).getById(contractIdOrExtId)
              .onItem().transform((container) -> ImmutableQueryEnvelope.<ContractContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(container)
                  .build());
        });
  }

  @Override
  public Uni<QueryEnvelopeList<ContractContainer>> findAll() {
    return this.state
        .onItem().transformToUni(state -> {
          return startQuery(state).findAll().collect().asList()
              .onItem().transform(items -> ImmutableQueryEnvelopeList.<ContractContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(items)
                  .build());
        });
  }
  
  
  public static ContractQueryImpl of(ContractDb db) {
    return new ContractQueryImpl(Uni.createFrom().item(db));
  }
}