import React from 'react';
import { ConfigOption, FsNode, useFs } from '@dxs-ts/fs-api';
import { FsDirentProps } from './FsDirentProps';
import { FsDirentClasses } from './useUtilityClasses';

export interface OwnerState {

  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: FsNode) => void;

  isChildError: (node: FsNode) => boolean;
  openAsset: (asset: FsNode, pathToTopParent: string) => void;

  node: FsNode;
  nodeIconClassName: keyof FsDirentClasses;
  level: number;
  parentPath?: string;
  fullPath: string;
  searchTerm: string;
  children: FsNode[];
  options: (keyof ConfigOption)[];

  
  isDarkMode: boolean;
  isChildren: boolean;
  configOptions: boolean;
  childWithError: boolean;
  showError: boolean;
}


export const useOwnerState = (props: FsDirentProps): OwnerState => {
  const { node, level, parentPath, onToggle, onContextMenu, searchTerm } = props;
  const { isDarkMode, isChildError, openAsset } = useFs();

  const isChildren = !!(node.children && node.children.length > 0);
  const configOptions = !!(node.configOptions && node.configOptions.length > 0);
  const childWithError = !!(isChildren && node.children!.some(child => isChildError(child)));
  const showError = !!(node.error || childWithError);
  const fullPath = parentPath ? `${parentPath} / ${node.name}` : node.name;

  return {
    node,
    nodeIconClassName: _getIconClassName(node),
    level,
    parentPath,
    searchTerm,
    fullPath,
    configOptions,

    isDarkMode,
    childWithError,
    showError,

    isChildren,
    children: _sortChildren(node.children ?? []),
    options: _getConfigOptions(node.configOptions ?? []),

    
    onToggle,
    onContextMenu,
    isChildError,
    openAsset
  };
};



function _sortChildren(children: FsNode[]) {
  const order = ['article', 'service', 'dialob', 'flow', 'link', 'language', 'printout', 'image', 'template'];
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}

function _getConfigOptions(options: ConfigOption[]) {
  return options.flatMap((opt) => Object.entries(opt)
      .filter(([_, value]) => value === true)
      .map(([key]) => key as keyof ConfigOption)
  )
}


function _getIconClassName(node: FsNode): keyof FsDirentClasses {
  switch (node.type) {
    case 'folder': return 'iconFolder';
    case 'article': return 'iconArticle';
    case 'service': return 'iconService';
    case 'dialob': return 'iconDialob';
    case 'flow': return 'iconFlow';
    case 'link': return 'iconLink';
    case 'language': return 'iconLanguage';
    case 'printout': return 'iconPrintout';
    case 'image': return 'iconImage';
    case 'template': return 'iconTemplate';
    default: return 'iconFolder';
  }
}