package io.resys.limaone.spi.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultErrorLog;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

public class FlowProgramExecutionPrettyPrint {

  @Value
  @Builder
  public static class TableConfig {
    @Builder.Default
    int minColumnWidth = 15;
    @Builder.Default  
    int maxColumnWidth = 50;
    @Builder.Default
    int maxTableWidth = 120;
    @Builder.Default
    String padding = " ";
    @Builder.Default
    boolean unicodeEnabled = true;
  }

  @Value
  @Builder
  public static class ColumnData {
    @NonNull
    String title;
    @NonNull
    @Singular
    List<String> entries;
    int calculatedWidth;
  }

  @Value
  @Builder  
  public static class StepBox {
    @NonNull
    String stepHeader;
    @NonNull
    ColumnData acceptsColumn;
    @NonNull
    ColumnData returnsColumn;
    int totalWidth;
  }

  @Value
  @Builder
  public static class FlowOutput {
    @NonNull
    String flowName;
    @NonNull
    String history;
    @NonNull
    @Singular
    List<String> errors;
    @NonNull
    @Singular  
    List<StepBox> stepBoxes;
  }

  public static String toVerticalAsciiTable(final FlowResult result, final Flow_AST ast) {
    final TableConfig config = TableConfig.builder().build();
    final FlowOutput output = buildFlowOutput(result, ast, config);
    return renderFlowOutput(output, config);
  }

  private static FlowOutput buildFlowOutput(final FlowResult result, final Flow_AST ast, final TableConfig config) {
    final List<String> errors = extractErrors(result);
    final List<StepBox> stepBoxes = buildStepBoxes(result.getLogs(), config);
    
    return FlowOutput.builder()
        .flowName(ast.getName())
        .history(result.getShortHistory())
        .errors(errors)
        .stepBoxes(stepBoxes)
        .build();
  }

  private static List<String> extractErrors(final FlowResult result) {
    final List<String> errors = new ArrayList<>();
    int errorIndex = 1;
    
    for (final FlowResultLog log : result.getLogs()) {
      for (final FlowResultErrorLog errorLog : log.getErrors()) {
        errors.add("ERROR " + errorIndex + ": " + errorLog.getMsg());
        errorIndex++;
      }
    }
    
    return errors;
  }

  private static List<StepBox> buildStepBoxes(final List<FlowResultLog> logs, final TableConfig config) {
    final List<StepBox> stepBoxes = new ArrayList<>();
    final Map<String, Integer> stepCounters = new HashMap<>();
    
    for (int i = 0; i < logs.size(); i++) {
      final FlowResultLog log = logs.get(i);
      final String stepId = log.getStepId();
      
      // Track loop iterations
      final int loopIndex = stepCounters.getOrDefault(stepId, 0) + 1;
      stepCounters.put(stepId, loopIndex);
      
      final String stepName = loopIndex > 1 ? stepId + "[" + loopIndex + "]" : stepId;
      final String stepHeader = "STEP {" + (i + 1) + "} " + stepName + " : " + log.getStatus();
      
      final ColumnData acceptsColumn = buildColumnData("ACCEPTS", log.getAccepts(), config);
      final ColumnData returnsColumn = buildColumnData("RETURNS", log.getReturns(), config);
      
      final int totalWidth = calculateTotalWidth(stepHeader, acceptsColumn, returnsColumn, config);
      
      stepBoxes.add(StepBox.builder()
          .stepHeader(stepHeader)
          .acceptsColumn(acceptsColumn)
          .returnsColumn(returnsColumn)
          .totalWidth(totalWidth)
          .build());
    }
    
    return stepBoxes;
  }

  private static ColumnData buildColumnData(final String title, final Map<String, ?> data, final TableConfig config) {
    final List<String> entries = data.entrySet().stream()
        .map(entry -> entry.getKey() + ": " + formatValue(entry.getValue(), config))
        .collect(Collectors.toList());
    
    final int maxEntryWidth = entries.stream()
        .mapToInt(String::length)
        .max()
        .orElse(title.length());
    
    final int calculatedWidth = Math.max(maxEntryWidth, config.getMinColumnWidth());
    
    return ColumnData.builder()
        .title(title)
        .entries(entries)
        .calculatedWidth(calculatedWidth)
        .build();
  }

  private static int calculateTotalWidth(final String stepHeader, final ColumnData acceptsColumn, 
                                       final ColumnData returnsColumn, final TableConfig config) {
    final int headerWidth = stepHeader.length();
    final int columnsWidth = acceptsColumn.getCalculatedWidth() + returnsColumn.getCalculatedWidth() + 3; // +3 for separators
    final int contentBasedWidth = Math.max(headerWidth, columnsWidth) + 4; // +4 for borders
    
    // Use the larger of content-based width or configured max width
    return Math.max(contentBasedWidth, config.getMaxTableWidth());
  }

  private static String renderFlowOutput(final FlowOutput output, final TableConfig config) {
    final StringBuilder sb = new StringBuilder();
    
    // Header section
    sb.append("FLOW NAME: ").append(output.getFlowName()).append("\n");
    sb.append("HISTORY: ").append(output.getHistory()).append("\n");
    
    // Errors section
    if (!output.getErrors().isEmpty()) {
      sb.append("\n");
      for (final String error : output.getErrors()) {
        sb.append(error).append("\n");
      }
    }
    
    sb.append("\n");
    
    // Step boxes
    for (final StepBox stepBox : output.getStepBoxes()) {
      sb.append(renderStepBox(stepBox, config)).append("\n");
    }
    
    return sb.toString();
  }

  private static String renderStepBox(final StepBox stepBox, final TableConfig config) {
    final StringBuilder sb = new StringBuilder();
    final int totalWidth = stepBox.getTotalWidth();
    
    // Calculate balanced column widths
    final int availableWidth = totalWidth - 3; // -3 for borders and separator
    final int baseAcceptsWidth = stepBox.getAcceptsColumn().getCalculatedWidth();
    final int baseReturnsWidth = stepBox.getReturnsColumn().getCalculatedWidth();
    final int totalContentWidth = baseAcceptsWidth + baseReturnsWidth;
    
    final int leftColWidth;
    final int rightColWidth;
    
    if (totalContentWidth < availableWidth) {
      // Distribute extra space proportionally, but ensure accepts gets at least 50 chars if possible
      final int extraSpace = availableWidth - totalContentWidth;
      final int minAcceptsWidth = Math.min(50, availableWidth / 2);
      
      if (baseAcceptsWidth < minAcceptsWidth) {
        final int acceptsBonus = Math.min(extraSpace, minAcceptsWidth - baseAcceptsWidth);
        leftColWidth = baseAcceptsWidth + acceptsBonus;
        rightColWidth = availableWidth - leftColWidth;
      } else {
        // Distribute proportionally
        final double acceptsRatio = (double) baseAcceptsWidth / totalContentWidth;
        leftColWidth = Math.max(baseAcceptsWidth, (int) (availableWidth * acceptsRatio));
        rightColWidth = availableWidth - leftColWidth;
      }
    } else {
      leftColWidth = baseAcceptsWidth;
      rightColWidth = baseReturnsWidth;
    }
    
    final String topBorder = config.isUnicodeEnabled() ? 
        "┌" + "─".repeat(totalWidth - 2) + "┐" :
        "+" + "-".repeat(totalWidth - 2) + "+";
    
    final String bottomBorder = config.isUnicodeEnabled() ? 
        "└" + "─".repeat(leftColWidth) + "┴" + "─".repeat(rightColWidth) + "┘" :
        "+" + "-".repeat(leftColWidth) + "+" + "-".repeat(rightColWidth) + "+";
    
    final String middleBorder = config.isUnicodeEnabled() ? 
        "├" + "─".repeat(leftColWidth) + "┬" + "─".repeat(rightColWidth) + "┤" :
        "+" + "-".repeat(leftColWidth) + "+" + "-".repeat(rightColWidth) + "+";
    
    final String separator = config.isUnicodeEnabled() ? "│" : "|";
    
    // Top border
    sb.append(topBorder).append("\n");
    
    // Step header
    sb.append(separator).append(" ").append(padRight(stepBox.getStepHeader(), totalWidth - 3)).append(separator).append("\n");
    
    // Column separator
    sb.append(middleBorder).append("\n");
    
    // Column headers
    sb.append(separator).append(" ").append(padRight(stepBox.getAcceptsColumn().getTitle(), leftColWidth - 1))
      .append(separator).append(" ").append(padRight(stepBox.getReturnsColumn().getTitle(), rightColWidth - 1))
      .append(separator).append("\n");
    
    // Another separator
    sb.append(middleBorder).append("\n");
    
    // Data rows
    final int maxRows = Math.max(stepBox.getAcceptsColumn().getEntries().size(), 
                                stepBox.getReturnsColumn().getEntries().size());
    
    for (int i = 0; i < maxRows; i++) {
      final String leftEntry = i < stepBox.getAcceptsColumn().getEntries().size() ? 
          stepBox.getAcceptsColumn().getEntries().get(i) : "";
      final String rightEntry = i < stepBox.getReturnsColumn().getEntries().size() ? 
          stepBox.getReturnsColumn().getEntries().get(i) : "";
      
      sb.append(separator).append(" ").append(padRight(leftEntry, leftColWidth - 1))
        .append(separator).append(" ").append(padRight(rightEntry, rightColWidth - 1))
        .append(separator).append("\n");
    }
    
    // Bottom border
    sb.append(bottomBorder);
    
    return sb.toString();
  }

  private static String formatValue(final Object value, final TableConfig config) {
    if (value == null) {
      return "-";
    }
    
    return value.toString().replace("\n", " ").replace("\r", " ");
  }

  private static String truncate(final String str, final int maxLength) {
    if (str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength - 3) + "...";
  }

  private static String padRight(final String str, final int width) {
    if (str.length() >= width) {
      return str;
    }
    return str + " ".repeat(width - str.length());
  }
}

