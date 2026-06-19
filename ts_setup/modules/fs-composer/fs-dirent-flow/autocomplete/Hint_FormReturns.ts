import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_FormReturns {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];

    if (!container.navDesc.node) {
      return result;
    }

    if (!HintUtils.isEmptyLine(container)) {
      return result;
    }

    const formAnc = HintUtils.findAncestor(container.navDesc.node, ['FLOW_TASK_FORM']);
    if (!formAnc) {
      return result;
    }

    const form = formAnc.value;
    const ref = HintUtils.get('ref', form);
    const returns = HintUtils.get('returns', form);

    if (!ref?.value || returns) {
      return result;
    }

    if (!HintUtils.isAfterChildrenOf(container, ref)) {
      return result;
    }

    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder
      .id("returns block")
      .addField("returns", { indent: 8, value: "|" })
      .addValue('\r\n          final String email = form.context("Email")')
      .build());

    return result;
  }
}
