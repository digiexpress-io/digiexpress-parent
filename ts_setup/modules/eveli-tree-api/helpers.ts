import React from 'react';
import { TreeNode, ContextMenuData } from './tree-types';

function collapseAllNodesInternal(nodes: TreeNode[]): TreeNode[] {
  return nodes.map((node) => ({
    ...node,
    expanded: false,
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
      return { ...node, expanded: !node.expanded };
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