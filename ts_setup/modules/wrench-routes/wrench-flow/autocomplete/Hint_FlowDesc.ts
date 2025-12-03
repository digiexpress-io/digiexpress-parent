import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

export class Hint_FlowDesc {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_DESC: HdesApi.NodeKeywordTypes = "description";
    const BEFORE: HdesApi.NodeKeywordTypes[] = ['inputs', 'tasks'];
    
    if (flow.description || !flow.id) {
      return result;
    }

    const before = BEFORE
      .filter(name => this.hasNonNull(name, flow))
      .map(name => this.get(name, flow));
      
    if (!this.isBefore(container, before)) {
      return result;
    }
    
    if (!this.isAfter(container, [flow.id])) {
      return result;
    }

    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id('description').addField(KEY_DESC).build());
    
    return result;
  }

  private static hasNonNull(name: string, node: any): boolean {
    return this.get(name, node) ? true : false;
  }
  
  private static get(keyword: string, node: any): any {
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  private static isBefore(container: Container, nodes: (any | undefined | null)[]): boolean {
    for (const current of nodes) {
      if (!current) {
        continue;
      }
      if (container.nav.currentLine >= current.start) {
        return false;
      }
    }
    return true;
  }

  private static isAfter(container: Container, nodes: any[]): boolean {
    for (const current of nodes) {
      if (!(container.nav.currentLine > current.end)) {
        return false;
      }
    }
    return true;
  }
}