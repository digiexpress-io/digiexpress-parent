package io.resys.limaone.spi.program;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultLog;

public class FlowProgramExecutionPrettyPrint {
  
  public static String toAsciiTable(FlowResult result, Flow_AST ast) {
    List<FlowResultLog> logs = result.getLogs();
    int colCount = logs.size();

    // 1. Identify all unique keys for vertical rows
    Set<String> allAcceptKeys = logs.stream().flatMap(l -> l.getAccepts().keySet().stream()).collect(Collectors.toCollection(TreeSet::new));
    Set<String> allReturnKeys = logs.stream().flatMap(l -> l.getReturns().keySet().stream()).collect(Collectors.toCollection(TreeSet::new));

    // 2. Precalculate Max Widths for each column
    // widths[0] = Label column, widths[1...N] = Log columns
    int[] widths = new int[colCount + 1];

    // Label Column (Column 0)
    widths[0] = Stream.concat(allAcceptKeys.stream(), allReturnKeys.stream())
            .mapToInt(s -> ("  " + s).length()).max().orElse(0);
    widths[0] = Math.max(widths[0], Math.max("FIELD".length(), "STATUS".length()));

    // Log Columns (Columns 1 to N)
    for (int i = 0; i < colCount; i++) {
        FlowResultLog log = logs.get(i);
        int max = log.getStepId().length();
        max = Math.max(max, log.getStatus().toString().length());
        
        // Check all values in Accepts and Returns for this specific log
        int finalI = i;
        int maxVal = Stream.concat(log.getAccepts().values().stream(), log.getReturns().values().stream())
                .mapToInt(v -> v == null ? 1 : v.toString().length()).max().orElse(0);
        
        widths[i + 1] = Math.max(max, maxVal);
    }

    // 3. Build Formatting Strings based on widths
    StringBuilder sb = new StringBuilder();
    String rowFormat = "| %-" + widths[0] + "s |" + 
        IntStream.range(1, widths.length).mapToObj(i -> " %-" + widths[i] + "s |").collect(Collectors.joining("")) + "%n";
    
    int totalLineLength = Arrays.stream(widths).sum() + (3 * widths.length) + 1;
    String separator = "-".repeat(totalLineLength) + "\n";

    // Header
    sb.append("NAME: ").append(ast.getName()).append("\n");
    sb.append("HISTORY: ").append(result.getShortHistory()).append("\n").append(separator);

    // Step ID Row
    Object[] headers = new Object[colCount + 1];
    headers[0] = "FIELD";
    for (int i = 0; i < colCount; i++) headers[i + 1] = logs.get(i).getStepId();
    sb.append(String.format(rowFormat, headers)).append(separator);

    // Status Row
    Object[] statuses = new Object[colCount + 1];
    statuses[0] = "STATUS";
    for (int i = 0; i < colCount; i++) statuses[i + 1] = logs.get(i).getStatus();
    sb.append(String.format(rowFormat, statuses));

    // Accepts Section
    if (!allAcceptKeys.isEmpty()) {
        sb.append(String.format("| %-" + (totalLineLength - 4) + "s |%n", "[ACCEPTS]"));
        for (String key : allAcceptKeys) {
            Object[] vals = new Object[colCount + 1];
            vals[0] = "  " + key;
            for (int i = 0; i < colCount; i++) vals[i + 1] = formatVal(logs.get(i).getAccepts().get(key));
            sb.append(String.format(rowFormat, vals));
        }
    }

    // Returns Section
    if (!allReturnKeys.isEmpty()) {
        sb.append(String.format("| %-" + (totalLineLength - 4) + "s |%n", "[RETURNS]"));
        for (String key : allReturnKeys) {
            Object[] vals = new Object[colCount + 1];
            vals[0] = "  " + key;
            for (int i = 0; i < colCount; i++) vals[i + 1] = formatVal(logs.get(i).getReturns().get(key));
            sb.append(String.format(rowFormat, vals));
        }
    }

    return sb.append(separator).toString();
}

private static String formatVal(Object val) {
    return val == null ? "-" : val.toString().replace("\n", " ");
}

}

