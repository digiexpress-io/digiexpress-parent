import React from 'react';
import { FsDirentConfigOption, FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentProps } from './FsDirentProps';
import { FsDirentClasses } from './useUtilityClasses';

export interface OwnerState {

  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: FsDirent) => void;

  isChildError: (dirent: FsDirent) => boolean;
  openAsset: (asset: FsDirent, pathToTopParent: string) => void;

  dirent: FsDirent;
  direntIconClassName: keyof FsDirentClasses;
  level: number;
  parentPath?: string;
  fullPath: string;
  searchTerm: string;
  children: FsDirent[];
  options: (keyof FsDirentConfigOption)[];

  
  isDarkMode: boolean;
  isChildren: boolean;
  configOptions: boolean;
  childWithError: boolean;
  showError: boolean;
}


export const useOwnerState = (props: FsDirentProps): OwnerState => {
  const { dirent, level, parentPath, onToggle, onContextMenu, searchTerm } = props;
  const { isDarkMode, isChildError, openAsset } = useFsNav();

  const isChildren = !!(dirent.children && dirent.children.length > 0);
  const configOptions = !!(dirent.configOptions && dirent.configOptions.length > 0);
  const childWithError = !!(isChildren && dirent.children!.some(child => isChildError(child)));
  const showError = !!((dirent.errors && dirent.errors.length > 0) || childWithError);
  const fullPath = parentPath ? `${parentPath} / ${dirent.name}` : dirent.name;

  return {
    dirent,
    direntIconClassName: _getIconClassName(dirent),
    level,
    parentPath,
    searchTerm,
    fullPath,
    configOptions,

    isDarkMode,
    childWithError,
    showError,

    isChildren,
    children: _sortChildren(dirent.children ?? []),
    options: _getConfigOptions(dirent.configOptions ?? []),

    
    onToggle,
    onContextMenu,
    isChildError,
    openAsset
  };
};



function _sortChildren(children: FsDirent[]) {
  const order = ['article', 'service', 'dialob', 'flow', 'link', 'language', 'printout', 'image', 'template'];
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}

function _getConfigOptions(options: FsDirentConfigOption[]) {
  return options.flatMap((opt) => Object.entries(opt)
      .filter(([_, value]) => value === true)
      .map(([key]) => key as keyof FsDirentConfigOption)
  )
}


function _getIconClassName(dirent: FsDirent): keyof FsDirentClasses {
  switch (dirent.type) {
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