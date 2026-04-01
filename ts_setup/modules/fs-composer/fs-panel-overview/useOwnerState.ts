import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsPanelOverviewProps } from './FsPanelOverviewProps';


export interface OverviewRow {
  id: string;
  name: string;
  type: Fs.Type;
  configOptions: Fs.ConfigOption[];
  lastDate: string;
  isChild: boolean;
}

export interface OwnerState {
  isDarkMode: boolean;
  rows: OverviewRow[];
}

function getLastDate(changes: Fs.DirentAsset['changes']): string {
  if (changes.length === 0) {
    return '—';
  }
  const update = [...changes].reverse().find(c => c.changeType === 'update');
  if (update) {
    return update.changeDate;
  }
  const create = changes.find(c => c.changeType === 'create');
  return create ? create.changeDate : '—';
}

export const useOwnerState = (_props: FsPanelOverviewProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { dirents, getDirent } = useFsDirent();

  const rows = React.useMemo((): OverviewRow[] => {
    const result: OverviewRow[] = [];
    dirents.forEach(dirent => {
      const asset = getDirent(dirent.id);
      result.push({
        id: dirent.id,
        name: dirent.name,
        type: dirent.type,
        configOptions: asset ? (asset.configOptions as Fs.ConfigOption[]) : [],
        lastDate: asset ? getLastDate(asset.changes) : '—',
        isChild: false,
      });
      dirent.children.forEach(child => {
        const childAsset = getDirent(child.id);
        result.push({
          id: child.id,
          name: child.name,
          type: child.type,
          configOptions: childAsset ? (childAsset.configOptions as Fs.ConfigOption[]) : [],
          lastDate: childAsset ? getLastDate(childAsset.changes) : '—',
          isChild: true,
        });
      });
    });
    return result;
  }, [dirents, getDirent]);

  return { isDarkMode, rows };
};
