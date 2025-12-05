import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';


export class Hint_TaskBodyMapping {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    if (!container.navDesc.node) {
      return result;
    }

    const node = container.navDesc.node;
    const isInputs = node.type === 'FLOW_TASK_ASSET_INPUTS';
    if (!isInputs) {
      return result;
    }

    const inputs = node.value;
    const target  = container.navDesc.node.parent?.value;
    const ref = target?.children["ref"];

    if (!target || !ref) {
      return result;
    }

    const linked: HdesApi.Entity<HdesApi.AstBody> | undefined = container.assetsQuery.findOne(ref.value);
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

    return result;
  }

}