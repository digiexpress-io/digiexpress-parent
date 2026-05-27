import React from 'react';
import { useFsDirent, useFsu } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelChangesProps } from './FsPanelChangesProps';
import { FsColors } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
  confirmOpen: boolean;
  setConfirmOpen: React.Dispatch<React.SetStateAction<boolean>>;
  getStatusColor: (status: string, isDarkMode: boolean) => string;
  changes: { id: string; name: string; fullPath: string; bodyType: string }[];
  onDiscard: (id: string) => void;
}

export const useOwnerState = (_props: FsPanelChangesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { allChanges, cancel } = useFsu();
  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const changes = allChanges
    .filter(change => change.isChanged)
    .map(change => {
      const dirent = getDirent(change.id);
      return {
        id: change.id,
        name: dirent?.name ?? change.id,
        fullPath: dirent?.fullPath ?? change.id,
        bodyType: change.bodyType,
      };
    });

  function onDiscard(id: string) {
    cancel(id);
  }

  return ({ isDarkMode, confirmOpen, setConfirmOpen, getStatusColor, changes, onDiscard });
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
