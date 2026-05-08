import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsPanelOverviewProps } from './FsPanelOverviewProps';


export interface OverviewRow {
  id: string;
  name: string;
  type: Fs.BodyType;
  configOptions: Fs.ConfigOption[];
  lastDate: string;
  depth: number;
}

export interface OwnerState {
  isDarkMode: boolean;
  rows: OverviewRow[];
}

function getLastDate(changes: Fs.PropsBase['changes']): string {
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

function collectRows(
  items: Fs.DirentBase[],
  getDirent: (id: string) => Fs.DirentBase | undefined,
  getArticleName: (id: string) => string | undefined,
  depth: number
): OverviewRow[] {
  return items.flatMap(dirent => {
    const asset = getDirent(dirent.id);
    const name = dirent.type === 'ARTICLE' ? (getArticleName(dirent.id) ?? dirent.name) : dirent.name;
    const row: OverviewRow = {
      id: dirent.id,
      name,
      type: dirent.type,
      configOptions: asset ? (asset.props?.configOptions as Fs.ConfigOption[] ?? []) : [],
      lastDate: asset?.props ? getLastDate(asset.props.changes) : '—',
      depth,
    };
    return [row, ...collectRows(dirent.children, getDirent, getArticleName, depth + 1)];
  });
}

export const useOwnerState = (_props: FsPanelOverviewProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { dirents, getDirent, getArticleName } = useFsDirent();

  const rows = React.useMemo(
    () => collectRows(dirents, getDirent, getArticleName, 0),
    [dirents, getDirent, getArticleName]
  );

  return { isDarkMode, rows };
};
