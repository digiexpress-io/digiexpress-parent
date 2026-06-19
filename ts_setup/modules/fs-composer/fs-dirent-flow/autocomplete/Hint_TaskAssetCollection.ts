import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';


export class Hint_TaskAssetCollection {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];

    const node = container.navDesc.node;
    if (!node) {
      return result;
    }

    const isChange = node.type === 'FLOW_TASK_ASSET_COLLECTION';
    const isUndefined = node.type === 'FLOW_TASK_ASSET' && (node.value.keyword === 'service' || node.value.keyword === 'decisionTable')
      && !HintUtils.isNonNull('collection', node.value) && HintUtils.isEmptyLine(container);

    if(isUndefined || isChange) {
      const builder1 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder1.id("collection: true").addField("collection", { indent: 8, value: "true" }).build());

      const builder2 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder2.id("collection: false").addField("collection", { indent: 8, value: "false" }).build());
    }
    
    return result;
  }
}
