import React from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelChangesProps } from './FsPanelChangesProps';
import { FsColors } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
  confirmOpen: boolean;
  setConfirmOpen: React.Dispatch<React.SetStateAction<boolean>>;
  getStatusColor: (status: string, isDarkMode: boolean) => string;
  changes: { id: string; name: string; status: string }[];
}

export const useOwnerState = (_props: FsPanelChangesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { dirents, getDirent } = useFsDirent();
  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const changes = dirents
    .map(dirent => getDirent(dirent.id))
    .filter(dirent => dirent && dirent.props && dirent.props?.changes.length > 0)
    .map(dirent => {
      const latest = dirent!.props!.changes[dirent!.props!.changes!.length - 1];
      return { id: dirent!.id, name: dirent!.id, status: latest.changeType };
    });

  return ({ isDarkMode, confirmOpen, setConfirmOpen, getStatusColor, changes });
}

function getStatusColor(status: string, isDarkMode: boolean) {
  switch (status) {
    case 'delete':
      return isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight;
    case 'create':
      return FsColors.semantic.success;
    case 'update':
      return isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight;
    default:
      return isDarkMode ? FsColors.dark.text : FsColors.light.text;
  }
};
