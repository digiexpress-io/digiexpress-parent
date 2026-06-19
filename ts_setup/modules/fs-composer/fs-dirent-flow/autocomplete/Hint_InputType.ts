import { languages } from 'monaco-editor';
import { Container, TYPES } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_InputType {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];

    const node = container.navDesc.node;
    if (!node) {
      return result;
    }

    const isUndefined = node.type === 'FLOW_INPUT' && !HintUtils.get("type", node.value) && HintUtils.isEmptyLine(container);
    const isChange = node.type === 'FLOW_INPUT_ELEMENT' && node.value.keyword === 'type';

    if(isUndefined || isChange) {
      for (const type of TYPES) {
        const builder = new CompletionItemBuilder(container.model, container.modelPosition);
        result.push(builder.id("type: " + type).addField("type", { indent: 4, value: type }).build());
      }
    }
    return result;
  }
}