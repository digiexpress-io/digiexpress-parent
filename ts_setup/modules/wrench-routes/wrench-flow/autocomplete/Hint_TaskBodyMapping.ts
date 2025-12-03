import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

interface TaskBodyPos {
  isEndOfLine: boolean;
  inTask: boolean;
}

export class Hint_TaskBodyMapping {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    // This hint needs to be called for each task, but we don't have the task context
    // in the container pattern. For now, let's iterate through all tasks.
    const tasks = flow.tasks ? Object.values(flow.tasks) : [];
    
    for (const task of tasks) {
      const taskResults = this.processTask(container, flow, task);
      result.push(...taskResults);
    }
    
    return result;
  }

  private static processTask(container: Container, flow: HdesApi.AstFlowRoot, task: HdesApi.AstFlowNode): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    const service: HdesApi.AstFlowNode | undefined = task["service"];
    const decisionTable: HdesApi.AstFlowNode | undefined = task["decisionTable"];
    const target = decisionTable ? decisionTable : service;
    
    if (!target) {
      return result;
    }
    
    const inputs = target.children["inputs"];
    if (!inputs) {
      return result;
    }
    
    const afterInputBlock = (container.nav.currentLine - 1) === inputs.end;
    if (!this.isInNode(container, inputs) && !afterInputBlock) {
      return result;
    }

    const ref = target.children["ref"];
    if (!ref) {
      return result;
    }
    
    let linked: HdesApi.Entity<HdesApi.AstBody> | undefined = container.assetsQuery.findOne(ref.value);
    if (!linked) {
      return result;
    }

    const headers = linked.ast?.headers.acceptDefs;
    if (!headers) {
      return result;
    }

    // Add missing mappings
    for (const typeDef of headers) {
      if (inputs.children[typeDef.name]) {
        continue;
      }
      
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder
        .id("add missing mapping: " + typeDef.name + " " + typeDef.valueType)
        .addField(typeDef.name, { indent: 10 })
        .build());
    }

    // Change mapping suggestions
    for (const [key, value] of Object.entries(inputs.children)) {
      if (value.end === container.nav.currentLine) {
        // Flow input suggestions
        for (const typeDef of container.flow.headers.acceptDefs) {
          const builder = new CompletionItemBuilder(container.model, container.modelPosition);
          result.push(builder
            .id("flow input: " + typeDef.name + " " + typeDef.valueType)
            .addField(key, { indent: 10, value: typeDef.name })
            .build());
        }

        // Task output suggestions
        const mappingEntries = this.getTaskBodyMappingEntry(container, flow, task, { key, value });
        result.push(...mappingEntries);
        break;
      }
    }

    return result;
  }

  private static getTaskBodyMappingEntry(container: Container, flow: HdesApi.AstFlowRoot, currentTask: HdesApi.AstFlowNode, props: { key: string, value: HdesApi.AstFlowNode }): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    for (const task of Object.values(flow.tasks)) {
      if (task.start > currentTask.start) {
        continue;
      }

      const service: HdesApi.AstFlowNode | undefined = task["service"];
      const decisionTable: HdesApi.AstFlowNode | undefined = task["decisionTable"];
      const target = decisionTable ? decisionTable : service;
      
      if (!target) {
        continue;
      }
      
      const ref = target.children["ref"];
      if (!ref) {
        continue;
      }
      
      let linked: HdesApi.Entity<HdesApi.AstBody> | undefined = container.assetsQuery.findOne(ref.value);
      if (!linked) {
        continue;
      }

      const headers = linked.ast?.headers.returnDefs;
      if (!headers) {
        continue;
      }

      for (const typeDef of headers) {
        const builder = new CompletionItemBuilder(container.model, container.modelPosition);
        result.push(builder
          .id("task output: " + task.id.value + "." + typeDef.name + " " + typeDef.valueType)
          .addField(props.key, { indent: 10, value: task.id.value + '.' + typeDef.name })
          .build());
      }
    }
    
    return result;
  }

  private static isInNode(container: Container, node: { start: number, end: number }): boolean {
    return container.nav.currentLine <= node.end && container.nav.currentLine >= node.start;
  }
}