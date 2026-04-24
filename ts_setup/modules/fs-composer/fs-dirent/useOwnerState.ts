import React from 'react';
import { Fs, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentProps } from './FsDirentProps';
import { FsDirentClasses } from './useUtilityClasses';

export interface OwnerState {

  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: Fs.DirentBase) => void;

  isChildError: (dirent: Fs.DirentBase) => boolean;
  openAsset: (asset: Fs.DirentBase, pathToTopParent: string) => void;

  dirent: Fs.DirentBase;
  direntIconClassName: keyof FsDirentClasses;
  level: number;
  parentPath?: string;
  fullPath: string;
  searchTerm: string;
  children: Fs.DirentBase[];
  options: Fs.ConfigOption[];

  
  isDarkMode: boolean;
  isActive: boolean;
  isChildren: boolean;
  configOptions: boolean;
  childWithError: boolean;
  showError: boolean;
}


export const useOwnerState = (props: FsDirentProps): OwnerState => {
  const { dirent, level, parentPath, onToggle, onContextMenu, searchTerm } = props;
  const { isDarkMode, openAsset, registerDirentPath, activeDirent } = useFsNav();
  const { isChildError, getDirent, creatableTypes } = useFsDirent();
  const direntProps = getDirent(dirent.id)?.props;

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
    isActive: activeDirent?.id === dirent.id,
    childWithError,
    showError,

    isChildren,
    children: _sortChildren(dirent.children ?? [], creatableTypes),
    options: direntProps?.configOptions ?? [],

    
    onToggle,
    onContextMenu,
    isChildError,
    openAsset
  };
};



function _sortChildren(children: Fs.DirentBase[], order: Fs.BodyType[]) {
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}


function _getIconClassName(dirent: Fs.DirentBase): keyof FsDirentClasses {
  switch (dirent.type) {
    case 'FOLDER': return 'iconFolder';
    case 'ARTICLE': return 'iconArticle';
    case 'ARTICLE_WORKFLOW': return 'iconService';
    case 'DIALOB_FORM': return 'iconDialob';
    case 'FLOW': return 'iconFlow';
    case 'ARTICLE_LINK': return 'iconLink';
    case 'LOCALE': return 'iconLanguage';
    case 'PRINTOUT': return 'iconPrintout';
    case 'PRINTOUT_PAGE': return 'iconImage';
    case 'ARTICLE_TEMPLATE': return 'iconTemplate';
    //case 'ARTICLE_PAGE': return 'iconPhone';
    case 'ARTICLE_PAGE': return 'iconPage';
    default: return 'iconFolder';
  }
}