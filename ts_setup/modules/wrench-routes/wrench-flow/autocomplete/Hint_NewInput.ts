import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';


export class Hint_NewInput {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const node = container.navDesc.node;

    if (!node) {
      return result;
    }

    if (!HintUtils.isEmptyLine(container)) {
      return result;
    }

    const isInInputsBlock = node.type === 'FLOW_INPUTS';
    const inputHasRequiredAndType = node.type === 'FLOW_INPUT' && HintUtils.isNonNull('required', node.value) && HintUtils.isNonNull('type', node.value);
    const isAfterCompleteInput = inputHasRequiredAndType && HintUtils.isAfterChildrenOf(container, node.value);

    if (!isInInputsBlock && !isAfterCompleteInput) {
      return result;
    }

    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id("new input")
      .addField("myInputParam", { indent: 2 })
      .addField("required", { indent: 4, value: "true" })
      .addField("type", { indent: 4, value: "STRING" })
      .addField("debugValue", { indent: 4, value: "\"test-string\"" })
      .build());

    return result;
  }
}