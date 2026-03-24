import React from 'react';
import { FsDirent, FsDirentType } from './fs-types';

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


export interface ConfigOption {
  value: string;
  label: string;
}

const ALL_CONFIG_OPTIONS: ConfigOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export function getConfigOptionsForType(type: FsDirentType): ConfigOption[] {
  switch (type) {
    case 'link':
    case 'phone': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode' || o.value === 'disabledMode');
    }
    case 'service':
    case 'article': {
      return ALL_CONFIG_OPTIONS;
    }
    case 'language': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode');
    }
    case 'printout': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode');
    }
    default: {
      return [];
    }
  }
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