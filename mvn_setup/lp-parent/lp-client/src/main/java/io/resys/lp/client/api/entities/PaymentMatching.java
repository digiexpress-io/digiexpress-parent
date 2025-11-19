package io.resys.lp.client.api.entities;

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.ledger.client.entities.Payment;


@Value.Immutable
public interface PaymentMatching {

  List<Payment> getMatches(); // Operation result
  List<Payment> getUnknowns(); // Operation result
  
}
