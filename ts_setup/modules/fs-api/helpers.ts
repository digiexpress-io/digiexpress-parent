import React from 'react';
import { FsNode, FsContextMenuData } from './fs-types';

function collapseAllNodesInternal(nodes: FsNode[]): FsNode[] {
  return nodes.map((node) => ({
    ...node,
    expanded: false,
    children: node.children ? collapseAllNodesInternal(node.children) : undefined,
  }));
}

export function collapseAll(
  fsData: FsNode[],
  setFsData: React.Dispatch<React.SetStateAction<FsNode[]>>
): void {
  setFsData(collapseAllNodesInternal(fsData));
}

function toggleNodeInternal(nodes: FsNode[], nodeId: string): FsNode[] {
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

export function toggleNode(nodeId: string, fsData: FsNode[], setFsData: React.Dispatch<React.SetStateAction<FsNode[]>>): void {
  setFsData(toggleNodeInternal(fsData, nodeId));
}

export function handleContextMenu( event: React.MouseEvent, node: FsNode,
  setContextMenuData: React.Dispatch<React.SetStateAction<FsContextMenuData | undefined>>,
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