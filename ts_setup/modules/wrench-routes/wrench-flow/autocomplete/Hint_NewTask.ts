import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';
import { HintUtils } from './HintUtils';

export class Hint_NewTask {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_TASKS: HdesApi.NodeKeywordTypes = "tasks";
    const tasks = HintUtils.get(KEY_TASKS, flow);
    
    if (tasks == null) {
      return result;
    }
    
    let isAround = tasks.start < container.nav.currentLine;
    let isEndOfLine = false;
    const allTasks: HdesApi.AstFlowNode[] = Object.values(tasks.children);
    
    for (const task of allTasks) {
      if (HintUtils.isEndOfLine(container, task)) {
        isEndOfLine = true;
        break;
      }
      if (HintUtils.isInNode(container, task)) {
        isAround = false;
      }
    }

    if (isAround || isEndOfLine) {
      // Switch task
      const builder1 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder1
        .id("new switch task")
        .append(isEndOfLine)
        .addField("- name", { indent: 2 })
        .addField("id", { indent: 6, value: "task-id" })
        .addField("switch", { indent: 6 })
        .addField("- caseName1", { indent: 8 })
        .addField("when", { indent: 12, value: "when-boolean-expression" })
        .addField("then", { indent: 12, value: "next-task-id" })
        .addField("- caseName2", { indent: 8 })
        .addField("when", { indent: 12, value: "when-boolean-expression" })
        .addField("then", { indent: 12, value: "next-task-id" })
        .build());

      // Service task
      const builder2 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder2
        .id("new service task")
        .append(isEndOfLine)
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" })
        .addField("{serviceType}", { indent: 6 })
        .addField("ref", { indent: 8, value: "{ref}" })
        .addField("collection", { indent: 8, value: "false" })
        .addField("inputs", { indent: 8 })
        .guided("service-task")
        .build());

      // Decision task
      const builder3 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder3
        .id("new decision task")
        .append(isEndOfLine)
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" })
        .addField("{serviceType}", { indent: 6 })
        .addField("ref", { indent: 8, value: "{ref}" })
        .addField("collection", { indent: 8, value: "false" })
        .addField("inputs", { indent: 8 })
        .guided("decision-task")
        .build());
    }
    
    return result;
  }
}