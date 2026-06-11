import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentProps } from './FsDirentProps';

export interface OwnerState {

  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: Fs.DirentBase) => void;

  isChildError: (dirent: Fs.DirentBase) => boolean;
  openAsset: (asset: Fs.DirentBase) => void;

  dirent: Fs.DirentBase;
  level: number;
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
  const { dirent, level, onToggle, onContextMenu, searchTerm, openAsset, activeDirentId } = props;
  const { isDarkMode } = useFsTheme();
  const { isChildError, getDirent } = useFsDirent();

  const children = (dirent.children ?? [])
    .map(c => {
      const full = getDirent(c.id);
      if (!full) {
        return undefined;
      }
      return { ...full, children: c.children };
    })
    .filter((c): c is Fs.DirentBase => !!c);

  const isChildren = children.length > 0;
  const configOptions = ((dirent?.props?.configOptions ?? []).length) > 0;
  const childWithError = !!(isChildren && dirent.children.some(child => {
    const d = getDirent(child.id);
    return d ? isChildError(d) : false;
  }));

  const showError = ((dirent?.props?.errors ?? []).length) > 0 || childWithError;
  return {
    dirent,
    level,
    searchTerm,
    configOptions,

    isDarkMode,
    isActive: activeDirentId === dirent.id,
    childWithError,
    showError,

    isChildren,
    children,
    options: dirent?.props?.configOptions ?? [],

    onToggle,
    onContextMenu,
    isChildError,
    openAsset
  };
};

