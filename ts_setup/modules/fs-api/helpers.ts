import React from 'react';
import { FsDirent } from './fs-types';

export function collectArticles(nodes: FsDirent[]): { value: string; label: string }[] {
  const result: { value: string; label: string }[] = [];
  nodes.forEach(node => {
    if (node.type === 'article') {
      result.push({ value: node.id, label: node.name });
    }
    if (node.children && node.children.length > 0) {
      result.push(...collectArticles(node.children));
    }
  });
  return result;
}


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