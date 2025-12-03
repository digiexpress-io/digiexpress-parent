import { HdesApi } from '@dxs-ts/wrench-api';
import { Container } from './Hint';

export class HintUtils {
  static get(keyword: string, node: any): any {
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  static isNonNull(name: string, node: any): boolean {
    return HintUtils.get(name, node) ? true : false;
  }

  static isInNode(container: Container, node: { start: number, end: number }, endNode?: { start: number, end: number }): boolean {
    let ending = node.end;
    if (endNode) {
      ending = endNode.start - 1;
    }
    return container.nav.currentLine <= ending && container.nav.currentLine >= node.start;
  }

  static isBefore(container: Container, nodes: (any | undefined | null)[]): boolean {
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

  static isAfter(container: Container, nodes: any[]): boolean {
    for (const current of nodes) {
      if (!(container.nav.currentLine > current.end)) {
        return false;
      }
    }
    return true;
  }

  static isEndOfLine(container: Container, node: HdesApi.AstFlowNode): boolean {
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