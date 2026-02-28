package io.resys.limaone.spi.compiler.decisiontable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.DecisionProgram.DecisionRow;
import io.resys.limaone.program.ImmutableDecisionRow;
import io.resys.limaone.program.ImmutableDecisionRowAccepts;
import io.resys.limaone.program.ImmutableDecisionRowReturns;
import io.resys.limaone.program.ImmutableProgramMessage;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramMessage;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.compiler.CompilableUnit;
import io.resys.limaone.spi.program.ProgramException;
import io.resys.limaone.spi.program.expression.ExpressionProgramFactory;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Compiler_DecisionTable implements CompilableUnit {
  private final AST_Parser parser;
  private final ModelWorld world;
  private final Model<DecisionTable> target;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    final DecisionTable_AST ast = parser.parseDecisionTable()
        .id(target.getId())
        .nodes(target.getBody().getNodes())
        .parse();
        
    return new OpenProgram() {
      @Override
      public String getId() {
        return ast.getId();
      }
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public Program close(Artifact artifact) {
        final List<ProgramAssociation> assocs = artifact.getAssociations();
        final List<ProgramMessage> errors = new ArrayList<>(artifact.getErrors());
        
        List<DecisionRow> rows;
        ProgramStatus status = artifact.getProgramStatus();
        try {
          rows = createRows(ast);
          status = ProgramStatus.UP;
        } catch(Exception e) {
          rows = Collections.emptyList();
          status = ProgramStatus.PROGRAM_ERROR;
          errors.add(ImmutableProgramMessage.builder()
              .exception(e)
              .id("decision-table-compiler-exception")
              .msg(e.getMessage())
              .build());
        }
        
        final var program = new DecisionProgramImpl(ast, status, rows, errors, assocs);
        
        return program;
      }
    };
  }
  
  private List<DecisionRow> createRows(DecisionTable_AST ast) {
    final List<DecisionRow> result = new ArrayList<>();    
    try {
      final var accepts = ast.getHeaders().getAcceptDefs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      final var returns = new HashMap<>(ast.getHeaders()
          .getReturnDefs().stream()
          .collect(Collectors.toMap(e -> e.getId(), e -> e)));
      
      accepts.values().forEach(e -> {
        returns.put("_" + e.getId(), e);
      });
      
      
      final List<DecisionTable_AST.DecisionRowNode> rows = new ArrayList<>(ast.getRows());
      Collections.sort(rows, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
      

      for(var row : rows) {

        final var programRow = ImmutableDecisionRow.builder().order(row.getOrder());
        for(final var value : row.getCells()) {
          
          if(accepts.containsKey(value.getHeader())) {
            if(value.getValue() == null || value.getValue().isBlank()) {
              continue;
            }
            final var typeDef = accepts.get(value.getHeader());
            programRow.addAccepts(ImmutableDecisionRowAccepts.builder()
                .key(typeDef)
                .expression(ExpressionProgramFactory.build(value.getValue(), typeDef.getValueType()))
                .build());
          } else {
            final var typeDef = returns.get(value.getHeader());
            if(value.getValue() == null && typeDef.getValueType() != ValueType.INTL) {
              continue;
            }
            
            try {
              programRow.addReturns(ImmutableDecisionRowReturns.builder()
                  .key(typeDef)
                  .value(typeDef.toValue(value.getValue()))
                  .build());
            } catch(Exception e) {
              throw new DecisionRowException(
                  row.getOrder(), typeDef.getOrder(),
                  "Failed to create expression: '" + value.getValue() + "'!" +
                  System.lineSeparator() + e.getMessage(), e);
              
            }
          }
        }
        result.add(programRow.build());
      }
      return result;
    } catch(ProgramException | DecisionRowException ex) {
      throw ex;
      
    } catch(Exception e) {
      throw new ProgramException(
          "Failed to create decision program from ast: '" + ast.getName() + "'!" +
          System.lineSeparator() + e.getMessage(), e);
    }
  }
}
