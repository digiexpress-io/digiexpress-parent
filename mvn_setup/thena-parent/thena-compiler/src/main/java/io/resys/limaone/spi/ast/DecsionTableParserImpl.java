package io.resys.limaone.spi.ast;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.Hashing;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.DecsionTableParser;
import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.model.DecisionTable.ColumnExpressionType;
import io.resys.limaone.model.DecisionTable.DecisionTableNode;
import io.resys.limaone.model.DecisionTable.DecisionTableNodeType;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.DecisionTable_AST_CacheKey;
import io.resys.limaone.spi.ast.decisiontable.CommandMapper;

public class DecsionTableParserImpl implements AST_Parser.DecsionTableParser {

  private final CommandMapper builder = new CommandMapper();
  private List<DecisionTableNode> src;
  private String id;

  @Override
  public DecsionTableParserImpl nodes(List<DecisionTableNode> src) {
    if(src == null) {
      return this;
    }
    
    this.src = src;
    return this;
  }
  @Override
  public DecsionTableParser id(String id) {
    this.id = id;
    return this;
  }

  @Override
  public DecisionTable_AST parse() {
    Objects.requireNonNull(src, () -> "src must be defined!");
    Objects.requireNonNull(id, () -> "id must be defined!");

    
    final var hashString = new StringBuilder();
    this.src.forEach(command -> {
      hashString.append(command.getType());
      if(command.getId() != null) {
        hashString.append("id: ").append(command.getId());
      }
      if(command.getValue() != null) {
        hashString.append("value: ").append(command.getValue());
      }
      hashString.append(";");
    });
    
    final var hash = Hashing.murmur3_128().hashString(hashString.toString(), StandardCharsets.UTF_8).toString();
    final var cacheKey = new DecisionTable_AST_CacheKey(hash);
    final Function<DecisionTable_AST_CacheKey, DecisionTable_AST> mappingFunction = (k) -> {
      this.src.forEach(command -> execute(command));
      return builder.build().id(id).hash(hash).build();
    };
    return LocalCache.computeIfAbsent(cacheKey, mappingFunction);
  }

  protected CommandMapper execute(DecisionTableNode command) {
    try {
      final DecisionTableNodeType type = command.getType();
      switch(type) {
      case SET_NAME:
        return builder.name(command.getValue());
      case SET_DESCRIPTION:
        return builder.description(command.getValue());
      case SET_HIT_POLICY:
        return builder.hitPolicy(!StringUtils.isEmpty(command.getValue()) ? HitPolicy.valueOf(command.getValue()) : null);
      case MOVE_ROW:
        // swap
        return builder.moveRow(command.getId(), command.getValue());
      case INSERT_ROW:
        // insert
        return builder.insertRow(command.getId(), command.getValue());
        
      case COPY_ROW:
        return builder.copyRow(command.getId());
      case MOVE_HEADER:
        return builder.moveHeader(command.getId(), command.getValue());
      case SET_HEADER_TYPE:
        return builder.changeHeaderType(command.getId(), command.getValue());
      case SET_HEADER_SCRIPT:
        return builder.changeHeaderScript(command.getId(), command.getValue());
      
      case SET_HEADER_REF:
        return builder.changeHeaderName(command.getId(), command.getValue());
      case SET_HEADER_EXTERNAL_REF:
        return builder.changeHeaderExtRef(command.getId(), command.getValue());
      case SET_HEADER_DIRECTION:
        return builder.changeHeaderDirection(command.getId(), Direction.valueOf(command.getValue()));
      case SET_HEADER_EXPRESSION:
        return builder.setHeaderExpression(command.getId(), ColumnExpressionType.valueOf(command.getValue()));
      case SET_CELL_VALUE:
        return builder.changeCell(command.getId(), command.getValue());
      case DELETE_CELL:
        return builder.deleteCell(command.getId());
      case DELETE_HEADER:
        return builder.deleteHeader(command.getId());
      case DELETE_ROW:
        return builder.deleteRow(command.getId());
      case ADD_HEADER_IN:
        return builder.addHeader(Direction.IN, command.getId() != null ? command.getId() : "").getValue();
      case ADD_HEADER_OUT:
        return builder.addHeader(Direction.OUT, command.getId() != null ? command.getId() : "").getValue();
      case ADD_ROW: {
        builder.addRow();
        return builder;
      }

      case IMPORT_ORDERED_CSV: {
        
        final var result = builder.deleteColumns().deleteRows();
        CSVFormat format = StringUtils.isNotEmpty(command.getId()) ?
          CSVFormat.DEFAULT.builder().setDelimiter(command.getId()).get() : CSVFormat.DEFAULT;
        CSVParser parser = CSVParser.parse(command.getValue(), format);
        List<CSVRecord> records = parser.getRecords();
        if(records.isEmpty()) {
          return result;
        }
        
        Iterator<CSVRecord> iterator = records.iterator();
        CSVRecord csvHeader = iterator.next();
        csvHeader.forEach(c -> {
          String id = result.addHeader(Direction.IN, "").getKey();
          result.changeHeaderType(id, ValueType.STRING.name());
          result.changeHeaderName(id, c);
        });

        while(iterator.hasNext()) {
          final CSVRecord row = iterator.next();
          final String rowId = result.addRow();
          final Iterator<String> cellIterator = row.iterator();
          
          int columnIndex = 0;
          while(cellIterator.hasNext()) {
            result.changeCell(rowId, columnIndex++, cellIterator.next());
          }
        }
        return result;
      }
      case IMPORT_CSV: 
        final var result = builder.deleteColumns().deleteRows();
        CSVParser parser = CSVParser.parse(command.getValue(), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        if(records.isEmpty()) {
          return result;
        }
        
        Iterator<CSVRecord> iterator = records.iterator();
        CSVRecord csvHeader = iterator.next();
        csvHeader.forEach(c -> {
          String id = result.addHeader(Direction.IN, "").getKey();
          result.changeHeaderType(id, ValueType.STRING.name());
          result.changeHeaderName(id, c);
        });

        while(iterator.hasNext()) {
          final CSVRecord row = iterator.next();
          int rowId = Integer.parseInt(result.addRow());
          final Iterator<String> cellIterator = row.iterator();
          while(cellIterator.hasNext()) {
            result.changeCell(String.valueOf(++rowId), cellIterator.next());
          }
        }
        return result;
      case SET_VALUE_SET:
        return builder.setValueSet(command.getId(), command.getValue());
      default: return builder;
      }
    } catch(AST_Exception e) {
      throw e;
    } catch(Exception e) {
      throw new AST_Exception(command, e.getMessage(), e);
    }
  }
}
