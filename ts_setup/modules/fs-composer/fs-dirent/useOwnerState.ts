import React from 'react';
import { Fs, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentProps } from './FsDirentProps';
import { FsDirentClasses } from './useUtilityClasses';

export interface OwnerState {

  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: Fs.DirentBase) => void;

  isChildError: (dirent: Fs.Dirent) => boolean;
  openAsset: (asset: Fs.Dirent, pathToTopParent: string) => void;

  dirent: Fs.Dirent;
  direntIconClassName: keyof FsDirentClasses;
  level: number;
  parentPath?: string;
  fullPath: string;
  searchTerm: string;
  children: Fs.Dirent[];
  options: Fs.ConfigOption[];

  isDarkMode: boolean;
  isActive: boolean;
  isChildren: boolean;
  configOptions: boolean;
  childWithError: boolean;
  showError: boolean;
}


export const useOwnerState = (props: FsDirentProps): OwnerState => {
  const { dirent: direntBase, level, parentPath, onToggle, onContextMenu, searchTerm } = props;
  const { isDarkMode, openAsset, registerDirentPath, activeDirent } = useFsNav();
  const { isChildError, getDirent, creatableTypes } = useFsDirent();

  const dirent = getDirent(direntBase.id) as Fs.Dirent;
  const children = (direntBase.children ?? [])
    .map(c => getDirent(c.id))
    .filter((c): c is Fs.Dirent => !!c);

  const isChildren = children.length > 0;
  const configOptions = ((dirent?.configOptions ?? []).length) > 0;
  const childWithError = !!(isChildren && direntBase.children.some(child => isChildError(child)));
  const showError = ((dirent?.errors ?? []).length) > 0 || childWithError;
  const fullPath = parentPath ? `${parentPath} / ${direntBase.name}` : direntBase.name;

  registerDirentPath(direntBase.id, fullPath);

  return {
    dirent,
    direntIconClassName: _getIconClassName(direntBase),
    level,
    parentPath,
    searchTerm,
    fullPath,
    configOptions,

    isDarkMode,
    isActive: activeDirent?.id === direntBase.id,
    childWithError,
    showError,

    isChildren,
    children: _sortChildren(children, creatableTypes),
    options: dirent?.configOptions ?? [],

    onToggle,
    onContextMenu,
    isChildError,
    openAsset
  };
};



function _sortChildren(children: Fs.Dirent[], order: Fs.BodyType[]) {
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
    case 'ARTICLE_TEMPLATE': return 'iconTemplate';
    case 'ARTICLE_PAGE': return 'iconPage';
    default: return 'iconFolder';
  }
}
