package io.resys.thena.contract.client.spi.queries;

import java.util.List;

import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ContractQueryActions.ContractQuery;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.entities.ContractDocType;
import io.resys.thena.contract.client.tables.ContractDb;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ContractQueryImpl implements ContractQuery {

  private final Uni<ContractDb> state;
  

  
  @Override
  public ContractQuery lockForUpdate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ContractQuery excludeDocs(ContractDocType... docs) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ContractQuery addContractId(String ids) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ContractQuery addAllContractId(List<String> ids) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<QueryEnvelope<ContractContainer>> get(String missionIdOrExtId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<QueryEnvelopeList<ContractContainer>> findAll() {
    // TODO Auto-generated method stub
    return null;
  }

  
  public static ContractQueryImpl of(ContractDb db) {
    return new ContractQueryImpl(Uni.createFrom().item(db));
  }
}
