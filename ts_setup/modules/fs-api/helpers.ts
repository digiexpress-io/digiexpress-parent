import React from 'react';
import { FsDirent, FsDirentContextMenuData } from './fs-types';

function collapseAllDirentsInternal(dirents: FsDirent[]): FsDirent[] {
  return dirents.map((dirent) => ({
    ...dirent,
    expanded: false,
    children: dirent.children ? collapseAllDirentsInternal(dirent.children) : [],
  }));
}

export function collapseAll(
  fsData: FsDirent[],
  setFsData: React.Dispatch<React.SetStateAction<FsDirent[]>>
): void {
  setFsData(collapseAllDirentsInternal(fsData));
}

function toggleDirentInternal(dirents: FsDirent[], direntId: string): FsDirent[] {
  return dirents.map((dirent) => {
    if (dirent.id === direntId) {
      return { ...dirent, expanded: !dirent.expanded };
    }
    if (dirent.children) {
      return { ...dirent, children: toggleDirentInternal(dirent.children, direntId) };
    }
    return dirent;
  });
}

export function toggleDirent(direntId: string, fsData: FsDirent[], setFsData: React.Dispatch<React.SetStateAction<FsDirent[]>>): void {
  setFsData(toggleDirentInternal(fsData, direntId));
}

export function handleContextMenu(event: React.MouseEvent, dirent: FsDirent,
  setContextMenuData: React.Dispatch<React.SetStateAction<FsDirentContextMenuData | undefined>>,
  setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>
): void {
  event.preventDefault();
  setContextMenuData({
    dirent,
    anchorPosition: {
      top: event.clientY,
      left: event.clientX,
    },
  });
  setContextMenuOpen(true);
}