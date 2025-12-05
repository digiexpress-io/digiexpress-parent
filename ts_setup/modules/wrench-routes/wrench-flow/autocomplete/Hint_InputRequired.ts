import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';


export class Hint_InputRequired {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    const node = container.navDesc.node;
    if (!node) {
      return result;
    }

    const isUndefined = node.type === 'FLOW_INPUT' && !HintUtils.get("required", node.value);
    const isChange = node.type === 'FLOW_INPUT_ELEMENT' && node.value.keyword === 'required';

    if(isUndefined || isChange) {
      const builder1 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder1.id("required: true").addField("required", { indent: 4, value: "true" }).build());
      
      const builder2 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder2.id("required: false").addField("required", { indent: 4, value: "false" }).build());
    }
    return result;
  }
}