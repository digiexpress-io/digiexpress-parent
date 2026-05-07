import { HdesApi } from '@dxs-ts/wrench-api';
import { Container } from './Hint';
import { AstNavNode, AstNavNodeType } from './AstNav';

export class HintUtils {
  static get(keyword: HdesApi.NodeKeywordTypes, node: HdesApi.AstFlowNode): any {
    // @ts-ignore
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  static isNonNull(name: HdesApi.NodeKeywordTypes, node: any): boolean {
    return HintUtils.get(name, node) ? true : false;
  }

  static isInNode(container: Container, node: { start: number, end: number }, endNode?: { start: number, end: number }): boolean {
    let ending = node.end;
    if (endNode) {
      ending = endNode.start - 1;
    }
    return container.nav.currentLine <= ending && container.nav.currentLine >= node.start;
  }


  // returns the nearest ancestor of `node` whose `type` is in `types`
  // useful when `navDesc.node` resolves to a deep block (e.g. FLOW_TASK_ASSET) but the hint cares about the enclosing FLOW_TASK or FLOW_TASKS context
  static findAncestor(node: AstNavNode | undefined, types: AstNavNodeType[]): AstNavNode | undefined {
    let current: AstNavNode | undefined = node;
    while (current) {
      if (types.includes(current.type)) {
        return current;
      }
      current = current.parent;
    }
    return undefined;
  }

  // true when the editor line under the cursor has no non-whitespace content
  static isEmptyLine(container: Container): boolean {
    return container.model.getLineContent(container.modelPosition.lineNumber).trim().length === 0;
  }

  
  // true when the cursor line is past the end line of every direct child of `node`
  // useful to define "after the node's contents"
  static isAfterChildrenOf(container: Container, node: HdesApi.AstFlowNode): boolean {
    const children = node.children ? Object.values(node.children) : [];
    if (children.length === 0) {
      return container.nav.currentLine > node.start;
    }
    return HintUtils.isAfter(container, children);
  }

  // true when a FLOW_TASK already has a body
  // used to decide whether a "new task" hint should insert the body for the current task or a brand-new task
  static taskHasBody(node: HdesApi.AstFlowNode): boolean {
    return HintUtils.isNonNull('service', node)
      || HintUtils.isNonNull('decisionTable', node)
      || HintUtils.isNonNull('switch', node)
      || HintUtils.isNonNull('userTask', node)
      || HintUtils.isNonNull('returns', node);
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