import React from 'react';
import { FsNode, useFs } from '@dxs-ts/fs-api';
import { FsExplorerNodeProps } from './FsExplorerNodeProps';

export interface OwnerState {
  node: FsNode;
  level: number;
  parentPath?: string;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: FsNode) => void;
  searchTerm: string;

  isDarkMode: boolean;
  isChildError: (node: FsNode) => boolean;
  openAsset: (asset: FsNode, pathToTopParent: string) => void;
  sortChildren: (children: FsNode[]) => FsNode[];
  children: boolean;
  configOptions: boolean;
  childWithError: boolean;
  showError: boolean;
  fullPath: string;
}

function sortChildren(children: FsNode[]) {
  const order = ['article', 'service', 'dialob', 'flow', 'link', 'language', 'printout', 'image', 'template'];
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}

export const useOwnerState = (props: FsExplorerNodeProps): OwnerState => {
  const { node, level, parentPath, onToggle, onContextMenu, searchTerm } = props;
  const { isDarkMode, isChildError, openAsset } = useFs();

  const children = !!(node.children && node.children.length > 0);
  const configOptions = !!(node.configOptions && node.configOptions.length > 0);
  const childWithError = !!(children && node.children!.some(child => isChildError(child)));
  const showError = !!(node.error || childWithError);
  const fullPath = parentPath ? `${parentPath} / ${node.name}` : node.name;

  return {
    node,
    level,
    parentPath,
    searchTerm,
    fullPath,
    configOptions,
    children,

    isDarkMode,
    childWithError,
    showError,

    onToggle,
    onContextMenu,
    sortChildren,
    isChildError,
    openAsset
  };
};