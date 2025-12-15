package io.resys.thena.ledger.client.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

/*-
 * #%L
 * thena-ledger-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerTreeNode;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerTreeNode_Default implements LedgerTreeNode {
  private final BlackBook blackBook;
  private final List<BlackBookDetail> blackBookDetails;
  private final LedgerTreeNode_Default next;
  private LedgerTreeNode_Default previous;

  @Override
  public BlackBook getBlackBook() {
    return blackBook;
  }
  @Override
  public List<BlackBookDetail> getBlackBookDetails() {
    return blackBookDetails;
  }
  @Override
  public List<LedgerTreeNode> getTill(String blackBookType) {
    final var result = new ArrayList<LedgerTreeNode>();
    LedgerTreeNode from  = this;
    while(from != null) {
      result.add(from);
      if(from.getBlackBook().getBookType().equals(blackBookType)) {
        break;
      }
      from = this.previous;
    }
    return Collections.unmodifiableList(result);
  }
  @Override
  public Optional<LedgerTreeNode> getPrevious() {
    return Optional.ofNullable(previous);
  }
  @Override
  public Optional<LedgerTreeNode> getNext() {
    return Optional.ofNullable(next);
  }
  @Override
  public Stream<LedgerTreeNode> getFrom(LocalDate targetDateInclusive) {
    Objects.requireNonNull(targetDateInclusive, () -> "targetDateInclusive cannot be null");
    
    return Stream.iterate(
      last(),
      node -> node != null && !node.getBlackBook().getBookDate().isBefore(targetDateInclusive),
      node -> node.getPrevious().orElse(null)
    );
  }
  
  public LedgerTreeNode_Default last() {
    if(this.next == null) {
      return this;
    }
    return this.next.last();
  }
  
  public void setParent(LedgerTreeNode_Default previous) {
    if(this.previous != null) {
      throw new IllegalArgumentException("previous is already set, can't change it!");
    }
    this.previous = previous;
  }
  
  public static LedgerTreeNode of(LedgerContainer ledger) {
    final var visited = new ArrayList<String>();
    final var bbs = ledger.getBlackBooks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    LedgerTreeNode_Default lastNode = null;
    LedgerTreeNode_Default next = null;
    
    
    var tipId = ledger.getLedger().getCurrentBlackBookId().orElseGet(null);
    while(tipId != null) {
      if(visited.contains(tipId)) {
        throw new IllegalArgumentException("Black book has wrong parent/child id, creates infinity!");
      }
      visited.add(tipId);
      
      final BlackBook bb = bbs.get(tipId);
      final List<BlackBookDetail> bbDetails = Optional
          .ofNullable(ledger.getBlackBookDetails().get(bb.getId()))
          .orElse(Collections.emptyList());
      
      final var target = new LedgerTreeNode_Default(bb, bbDetails, next);
      if(next != null) {
        next.setParent(target);
      }
      
      if(lastNode == null) {
        lastNode = target;
      }
      next = target;
    }
    return lastNode;
  }
}
