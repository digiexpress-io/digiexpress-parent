package io.resys.limaone.spi.dialob.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DialobFormPrettyPrinter {

      public String printForm(JsonObject form) {
          final var output = new StringBuilder();

          printFormHeader(form, output);
          printContextVariables(form, output);
          printValueSetsSummary(form, output);
          printFormStructure(form, output);

          return output.toString();
      }

      private void printFormHeader(JsonObject form, StringBuilder output) {
          final var id = form.getString("_id", "unknown");
          final var rev = form.getString("_rev", "1");
          final var name = form.getString("name", "unnamed");
          final var metadata = form.getJsonObject("metadata");

          output.append("📋 ").append(name).append(" (").append(id).append(") v").append(rev).append("\n");

          if (metadata != null) {
              final var languages = metadata.getJsonArray("languages");
              final var defaultLang = metadata.getString("defaultActiveLanguage", "unknown");
              output.append("├─ 🌐 Languages: ").append(languages).append(" (default: ").append(defaultLang).append(")\n");

              final var tenantId = metadata.getString("tenantId");
              if (tenantId != null) {
                  output.append("├─ 🏢 Tenant: ").append(tenantId).append("\n");
              }

              final var labels = metadata.getJsonArray("labels");
              if (labels != null) {
                  output.append("├─ 🏷️  Labels: ").append(labels).append("\n");
              }
          }
          output.append("│\n");
      }

      private void printContextVariables(JsonObject form, StringBuilder output) {
          final var variables = form.getJsonArray("variables");
          if (variables == null || variables.isEmpty()) return;

          output.append("├─ 📊 Context Variables (").append(variables.size()).append("):\n");

          for (int i = 0; i < variables.size(); i++) {
              final var variable = variables.getJsonObject(i);
              final var name = variable.getString("name");
              final var type = variable.getString("contextType", "unknown");
              final var defaultValue = variable.getValue("defaultValue");
              final var isLast = i == variables.size() - 1;

              output.append("│  ").append(isLast ? "└─ " : "├─ ")
                    .append(name).append(": ").append(type);

              if (defaultValue != null) {
                  output.append(" = \"").append(defaultValue).append("\"");
              }
              output.append("\n");
          }
          output.append("│\n");
      }

      private void printValueSetsSummary(JsonObject form, StringBuilder output) {
          final var valueSets = form.getJsonArray("valueSets");
          if (valueSets == null || valueSets.isEmpty()) return;

          output.append("├─ 📋 Value Sets (").append(valueSets.size()).append("):\n");

          final int showCount = Math.min(3, valueSets.size());
          for (int i = 0; i < showCount; i++) {
              final var vs = valueSets.getJsonObject(i);
              final var id = vs.getString("id");
              final var entries = vs.getJsonArray("entries");
              final var entryCount = entries != null ? entries.size() : 0;

              final var desc = getValueSetDescription(entries);

              output.append("│  ├─ ").append(id).append(": ").append(desc)
                    .append(" (").append(entryCount).append(" entries)\n");
          }

          if (valueSets.size() > showCount) {
              output.append("│  └─ ... (").append(valueSets.size() - showCount).append(" more)\n");
          }
          output.append("│\n");
      }

      private void printFormStructure(JsonObject form, StringBuilder output) {
          final var data = form.getJsonObject("data");
          if (data == null) return;

          output.append("└─ 🏗️  Form Structure:\n");

          final var questionnaire = data.getJsonObject("questionnaire");
          if (questionnaire != null) {
              printField("questionnaire", questionnaire, data, output, "   ", true, 0);
          }
      }

      private void printField(String fieldId, JsonObject field, JsonObject allData,
                            StringBuilder output, String indent, boolean isLast, int depth) {

          final var type = field.getString("type", "unknown");
          final var label = getFieldLabel(field);
          final var icon = getFieldIcon(type, field);
          final var props = getFieldProps(field, type);
          final var required = isRequired(field);
          final var defaultValue = field.getString("defaultValue");

          // Print field line
          output.append(indent).append(isLast ? "└─ " : "├─ ")
                .append(icon).append(" ").append(fieldId);

          // Add type and properties
          if (!type.equals("group") && !type.equals("questionnaire")) {
              output.append(" [").append(type);
              if (!props.isEmpty()) {
                  output.append(":").append(String.join(",", props));
              }
              output.append("]");
          } else if (!props.isEmpty()) {
              output.append(" [").append(String.join(",", props)).append("]");
          }

          // Add label
          if (label != null && !label.isEmpty()) {
              output.append(" \"").append(truncate(label, 30)).append("\"");
          }

          // Add indicators
          if (required) output.append(" ⭐");
          if (defaultValue != null) output.append(" = \"").append(truncate(defaultValue, 20)).append("\"");

          output.append("\n");

          // Print activeWhen condition
          final var activeWhen = field.getString("activeWhen");
          if (activeWhen != null) {
              final var conditionIndent = indent + (isLast ? "   " : "│  ");
              output.append(conditionIndent).append("│  ⚡ when: ")
                    .append(formatExpression(activeWhen)).append("\n");
          } else if (depth > 1) { // Only show for nested items
              final var conditionIndent = indent + (isLast ? "   " : "│  ");
              output.append(conditionIndent).append("│  ⚡ when: (always visible)\n");
          }

          // Print required rule if complex
          final var requiredRule = field.getValue("required");
          if (requiredRule != null && !requiredRule.equals("true") && !requiredRule.equals(true)) {
              final var conditionIndent = indent + (isLast ? "   " : "│  ");
              output.append(conditionIndent).append("└─ 🎯 required: ")
                    .append(formatExpression(requiredRule.toString())).append("\n");
          } else if (required && activeWhen != null) {
              final var conditionIndent = indent + (isLast ? "   " : "│  ");
              output.append(conditionIndent).append("└─ 🎯 required: true\n");
          }

          // Print validation rules
          printValidations(field, indent, isLast, output);

          // Print child items
          final var items = field.getJsonArray("items");
          if (items != null) {
              final var childIndent = indent + (isLast ? "   " : "│  ");
              for (int i = 0; i < items.size(); i++) {
                  final var itemId = items.getString(i);
                  final var childField = allData.getJsonObject(itemId);
                  if (childField != null) {
                      if (activeWhen != null || requiredRule != null) {
                          output.append(childIndent).append("│\n");
                      }
                      printField(itemId, childField, allData, output, childIndent,
                                i == items.size() - 1, depth + 1);
                  }
              }
          }
      }

      private String getFieldIcon(String type, JsonObject field) {
          final var props = field.getJsonObject("props");
          final var controlType = props != null ? props.getString("controlType") : null;
          final var view = field.getString("view");

          return switch (type) {
              case "text" -> {
                  if ("fileUpload".equals(controlType)) yield "📎";
                  if ("textBox".equals(view)) yield "📝";
                  yield "📝";
              }
              case "number", "decimal" -> "🔢";
              case "boolean" -> {
                  final var display = props != null ? props.getString("display") : null;
                  yield "checkbox".equals(display) ? "☑️" : "⚪";
              }
              case "date" -> "📅";
              case "time" -> "⏰";
              case "choice" -> "📋";
              case "multichoice" -> "☑️";
              case "survey" -> "📊";
              case "rowgroup" -> "📋";
              case "group" -> "📦";
              case "note" -> "📝";
              case "questionnaire" -> "📄";
              case "page" -> "📄";
              default -> "❓";
          };
      }

      private List<String> getFieldProps(JsonObject field, String type) {
          var props = new ArrayList<String>();
          final var valueSetId = field.getString("valueSetId");
          final var view = field.getString("view");
          final var propsObj = field.getJsonObject("props");

          if (valueSetId != null) props.add(valueSetId);
          if (view != null && !view.equals(type)) props.add(view);

          if (propsObj != null) {
              if (propsObj.getString("controlType") != null) props.add(propsObj.getString("controlType"));
              if (propsObj.getString("display") != null) props.add(propsObj.getString("display"));
              if (propsObj.getValue("border") != null) props.add("border");
              if (propsObj.getValue("collapsible") != null) props.add("collapsible");
              if (propsObj.getValue("noPrint") != null) props.add("noPrint");
          }

          return props;
      }

      private String formatExpression(String expression) {
          if (expression.length() <= 50) return expression;

          // Try to break at logical operators
          final var formatted = expression
              .replaceAll(" and ", "\n      and ")
              .replaceAll(" or ", "\n      or ");

          if (formatted.split("\n").length > 1) {
              return formatted.split("\n")[0] + "...";
          }

          return truncate(expression, 50);
      }

      private void printValidations(JsonObject field, String indent, boolean isLast, StringBuilder output) {
          // This would extract and format validation rules if they exist
          // Implementation depends on how validations are stored in the JSON
      }

      private String getValueSetDescription(JsonArray entries) {
          if (entries == null || entries.isEmpty()) return "entries";

          final var firstEntry = entries.getJsonObject(0);
          final var label = firstEntry.getJsonObject("label");
          if (label != null) {
              final var desc = label.getString("fi", label.getString("en", "entries"));
              return desc.length() > 30 ? desc.substring(0, 27) + "..." : desc;
          }
          return "entries";
      }

      private String getFieldLabel(JsonObject field) {
          final var label = field.getJsonObject("label");
          if (label == null) return null;
          return label.getString("fi", label.getString("en", null));
      }

      private boolean isRequired(JsonObject field) {
          final var required = field.getValue("required");
          return required != null && (required.equals("true") || required.equals(true));
      }

      private String truncate(String text, int maxLength) {
          if (text.length() <= maxLength) return text;
          return text.substring(0, maxLength - 3) + "...";
      }
  }