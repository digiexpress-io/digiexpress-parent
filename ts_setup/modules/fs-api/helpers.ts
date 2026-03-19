import React from 'react';
import { FsDirent } from './fs-types';


interface FsDirentContextMenuData {
  dirent: FsDirent;
  anchorPosition: { top: number; left: number };
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