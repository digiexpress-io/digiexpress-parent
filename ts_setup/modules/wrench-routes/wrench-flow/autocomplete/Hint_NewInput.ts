import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

export class Hint_NewInput {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_INPUTS: HdesApi.NodeKeywordTypes = "inputs";
    const KEY_TASKS: HdesApi.NodeKeywordTypes = "tasks";
    
    const inputs = this.get(KEY_INPUTS, flow);
    if (!inputs) {
      return result;
    }

    let isAround = this.isInNode(container, inputs, this.get(KEY_TASKS, flow));
    let isEndOfLine = false;
    const allInputs: HdesApi.AstFlowNode[] = Object.values(inputs.children);
    for (const input of allInputs) {
      if (this.isEndOfLine(container, input)) {
        isEndOfLine = true;
        break;
      }
      if (this.isInNode(container, input)) {
        isAround = false;
      }
    }

    if (isAround || isEndOfLine) {
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder.id("new input")
        .append(isEndOfLine)
        .addField("{name}", { indent: 2 })
        .addField("required", { indent: 4, value: "true" })
        .addField("type", { indent: 4, value: "STRING" })
        .addField("debugValue", { indent: 4, value: "\"test-string\"" })
        .build());
    }
    
    return result;
  }

  private static get(keyword: string, node: any): any {
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  private static isInNode(container: Container, node: { start: number, end: number }, endNode?: { start: number, end: number }): boolean {
    let ending = node.end;
    if (endNode) {
      ending = endNode.start - 1;
    }
    return container.nav.currentLine <= ending && container.nav.currentLine >= node.start;
  }

  private static isEndOfLine(container: Container, node: HdesApi.AstFlowNode): boolean {
    const sameLine = node.end === container.nav.currentLine;
    if (!sameLine) {
      return false;
    }

    const last = Object.values(node.children).filter(v => v.end === node.end).reduce(v => v);
    if (!last) {
      return container.nav.currentColumn >= node.value.length;
    }
    return container.nav.currentColumn >= last.source.value.length;
  }
}