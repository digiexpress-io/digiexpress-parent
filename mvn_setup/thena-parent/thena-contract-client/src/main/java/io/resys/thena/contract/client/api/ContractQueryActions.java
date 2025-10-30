package io.resys.thena.contract.client.api;

import java.util.List;

import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.entities.ContractDocType;
import io.smallrye.mutiny.Uni;

public interface ContractQueryActions {

  
  ContractQuery contractQuery();
  
  interface ContractQuery {
    
    ContractQuery lockForUpdate();
    
    // optimization, exclude explicitly doc-s that we don't need 
    ContractQuery excludeDocs(ContractDocType... docs);
    
    ContractQuery addContractId(String ids);
    ContractQuery addAllContractId(List<String> ids); // include only data for given contract
    
    Uni<QueryEnvelope<ContractContainer>> get(String ids);
    Uni<QueryEnvelopeList<ContractContainer>> findAll();
  }
  
}
