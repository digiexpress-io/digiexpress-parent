package io.resys.hdes.client.spi.decision.ast;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import io.resys.hdes.client.spi.decision.ast.CommandMapper.MutableCell;
import io.resys.hdes.client.spi.decision.ast.CommandMapper.MutableHeader;
import io.resys.hdes.client.spi.decision.ast.CommandMapper.MutableRow;
import io.resys.hdes.client.spi.util.HdesAssert;

public class IdFixer {
  

  private long idGen = 0;
  private final HashMap<String, MutableHeader> headers = new LinkedHashMap<>();
  private final Map<String, MutableCell> cells = new LinkedHashMap<>();
  private final Map<String, MutableRow> rows = new LinkedHashMap<>();

  public MutableHeader addHeader() {
    MutableHeader header = new MutableHeader(nextId(), null, headers.size());
    
    getHeaders().put(header.getId(), header);
    this.rows.values().stream().forEach(row -> {
      MutableCell cell = new MutableCell(nextId(), row.getId());
      header.getCells().add(cell);
      getCells().put(cell.getId(), cell);
    });
    return header;    
  }
  
  public void deleteHeader(String id) {
    getHeader(id).getCells().forEach(c -> cells.remove(c.getId()));
    getHeaders().remove(id);
  }

  public MutableRow addRow() {
    MutableRow row = new MutableRow(nextId(), rows.size());
    rows.put(row.getId(), row);
    
    getHeaders().values().stream().forEach(h -> {
      MutableCell cell = new MutableCell(nextId(), row.getId());
      h.getCells().add(cell);
      cells.put(cell.getId(), cell);
    });
    
    return row;
  }
  public void deleteRow(String id) {
    MutableRow row = getRow(id);
    rows.remove(row.getId());
    int order = row.getOrder();
    rows.values().stream()
    .filter(r -> r.getOrder() > order)
    .forEach(r -> r.setOrder(r.getOrder() - 1));
    
    getHeaders().values().forEach(h -> {
      Iterator<MutableCell> cell = h.getCells().iterator();
      while(cell.hasNext()) {
        if(id.equals(cell.next().getRow())) {
          cell.remove();
        }
      }
    });
    
  }
  public void deleteRows() {
    rows.keySet().forEach(id -> deleteRow(id));
  }
  
  public String nextId() {      
    return String.valueOf(idGen++);
  }
  public MutableHeader getHeader(String id) {
    HdesAssert.isTrue(headers.containsKey(id), () -> "no header with id: " + id + "!");
    return headers.get(id);
  }
  public MutableCell getCell(String id) {
    HdesAssert.isTrue(cells.containsKey(id), () -> "no cell with id: " + id + "!");
    return cells.get(id);
  }
  public MutableRow getRow(String id) {
    HdesAssert.isTrue(rows.containsKey(id), () -> "no row with id: " + id + "!");
    return rows.get(id);
  }
  public HashMap<String, MutableHeader> getHeaders() {
    return headers;
  }
  
  public void addRow(MutableRow row) {
    this.rows.put(row.getId(), row);;
  }
  public void removeHeader(String id) {
    getHeader(id).getCells().forEach(c -> removeCell(c));
    headers.remove(id);
  }
  
  public void removeCell(MutableCell c) {
    cells.remove(c.getId());
  }
  
  public void addCell(MutableCell cell) {
    this.cells.put(cell.getId(), cell);
  }
  
  public void addHeader(MutableHeader header) {
    headers.put(header.getId(), header);
  }
  public Map<String, MutableRow> getRows() {
    return rows;
  }
  public Map<String, MutableCell> getCells() {
    return cells;
  }
}
