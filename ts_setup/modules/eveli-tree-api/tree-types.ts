// Mock data structure for EveliTree component
export interface TreeNode {
  id: string;
  name: string;
  description?: string;
  children?: TreeNode[];
  isExpanded?: boolean;
  isReference?: boolean;
  type: TreeNodeType;
}

export type TreeNodeType = 'folder' | 'article' | 'service' | 'dialob' | 'flow' | 'link' | 'language' | 'printout' | 'image' | 'template';

export interface ContextMenuData {
  node: TreeNode;
  anchorPosition: { top: number; left: number };
}

function collapseAllNodesInternal(nodes: TreeNode[]): TreeNode[] {
  return nodes.map((node) => ({
    ...node,
    isExpanded: false,
    children: node.children ? collapseAllNodesInternal(node.children) : undefined,
  }));
}

export function collapseAll(
  treeData: TreeNode[],
  setTreeData: React.Dispatch<React.SetStateAction<TreeNode[]>>
): void {
  setTreeData(collapseAllNodesInternal(treeData));
}

function toggleNodeInternal(nodes: TreeNode[], nodeId: string): TreeNode[] {
  return nodes.map((node) => {
    if (node.id === nodeId) {
      return { ...node, isExpanded: !node.isExpanded };
    }
    if (node.children) {
      return { ...node, children: toggleNodeInternal(node.children, nodeId) };
    }
    return node;
  });
}

export function toggleNode(nodeId: string, treeData: TreeNode[], setTreeData: React.Dispatch<React.SetStateAction<TreeNode[]>>): void {
  setTreeData(toggleNodeInternal(treeData, nodeId));
}

export function handleContextMenu( event: React.MouseEvent, node: TreeNode,
  setContextMenuData: React.Dispatch<React.SetStateAction<ContextMenuData | undefined>>,
  setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>
): void {
  event.preventDefault();
  setContextMenuData({
    node,
    anchorPosition: {
      top: event.clientY,
      left: event.clientX,
    },
  });
  setContextMenuOpen(true);
}