import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';
import { HintUtils } from './HintUtils';


export class Hint_TaskInputMapping {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    if (!container.navDesc.node) {
      return result;
    }

    const node = container.navDesc.node;
    
    const isInput = node.type === 'FLOW_TASK_ASSET_INPUT';
    if (!isInput) {
      return result;
    }

    const target = container.navDesc.node.parent?.parent?.value;
    const ref = target?.children["ref"];

    if (!target || !ref) {
      return result;
    }

    const linked: HdesApi.Entity<HdesApi.AstBody> | undefined = container.assetsQuery.findOne(ref.value);
    if (!linked) {
      return result;
    }

    const input = node.value;
    for (const typeDef of container.flow.headers.acceptDefs) {
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder
        .id("flow input: " + typeDef.name + " " + typeDef.valueType)
        .addField(input.keyword, { indent: 10, value: typeDef.name })
        .build());
    }

    for (const task of Object.values(container.flow.parseTree.tasks)) {
      if (task.start > node.start) {
        continue;
      }

      const form = HintUtils.get('form', task);
      if (form) {
        const taskId = task.id?.value;
        if (!taskId) {
          continue;
        }

        for (const output of HintUtils.getFormOutputFields(container, task)) {
          const builder = new CompletionItemBuilder(container.model, container.modelPosition);
          result.push(builder
            .id("task output: " + taskId + "." + output.name + " " + output.type)
            .addField(input.keyword, { indent: 10, value: taskId + '.' + output.name })
            .build());
        }
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
          .addField(input.keyword, { indent: 10, value: task.id.value + '.' + typeDef.name })
          .build());
      }
    }
    
    return result;
  }
}