import React from 'react';
import { FsDirent, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentProps } from './FsDirentProps';
import { FsDirentClasses } from './useUtilityClasses';

export interface OwnerState {

  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: FsDirent.Dirent) => void;

  isChildError: (dirent: FsDirent.Dirent) => boolean;
  openAsset: (asset: FsDirent.Dirent, pathToTopParent: string) => void;

  dirent: FsDirent.Dirent;
  direntIconClassName: keyof FsDirentClasses;
  level: number;
  parentPath?: string;
  fullPath: string;
  searchTerm: string;
  children: FsDirent.Dirent[];
  options: FsDirent.ConfigOption[];

  
  isDarkMode: boolean;
  isChildren: boolean;
  configOptions: boolean;
  childWithError: boolean;
  showError: boolean;
}


export const useOwnerState = (props: FsDirentProps): OwnerState => {
  const { dirent, level, parentPath, onToggle, onContextMenu, searchTerm } = props;
  const { isDarkMode, openAsset, registerDirentPath } = useFsNav();
  const { isChildError, getDirent } = useFsDirent();
  const direntProps = getDirent(dirent.id);

  const isChildren = !!(dirent.children && dirent.children.length > 0);
  const configOptions = (direntProps?.configOptions.length ?? 0) > 0;
  const childWithError = !!(isChildren && dirent.children!.some(child => isChildError(child)));
  const showError = (direntProps?.errors.length ?? 0) > 0 || childWithError;
  const fullPath = parentPath ? `${parentPath} / ${dirent.name}` : dirent.name;

  registerDirentPath(dirent.id, fullPath);

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
    options: direntProps?.configOptions ?? [],

    
    onToggle,
    onContextMenu,
    isChildError,
    openAsset
  };
};



function _sortChildren(children: FsDirent.Dirent[]) {
  const order = ['article', 'service', 'dialob', 'flow', 'link', 'language', 'printout', 'image', 'template'];
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}


function _getIconClassName(dirent: FsDirent.Dirent): keyof FsDirentClasses {
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
    case 'phone': return 'iconPhone';
    default: return 'iconFolder';
  }
}