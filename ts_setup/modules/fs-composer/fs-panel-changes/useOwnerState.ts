import React from 'react';
import { useFsDirent, useFsu } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelChangesProps } from './FsPanelChangesProps';


export interface OwnerState {
  isDarkMode: boolean;
  confirmOpen: boolean;
  setConfirmOpen: React.Dispatch<React.SetStateAction<boolean>>;
  changes: { id: string; name: string; fullPath: string; bodyType: string }[];
  onSave: (id: string) => Promise<void>;
  onSaveAll: () => Promise<void>;
  onDiscard: (id: string) => void;
  onDiscardAll: () => void;
}

export const useOwnerState = (_props: FsPanelChangesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { allChanges, push, cancel } = useFsu();
  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const dirtyChanges = allChanges.filter(change => change.isDirty);

  const changes = dirtyChanges.map(change => {
    const dirent = getDirent(change.id);
    return {
      id: change.id,
      name: dirent?.name ?? change.id,
      fullPath: dirent?.fullPath ?? change.id,
      bodyType: change.bodyType,
    };
  });

  async function onSave(id: string) {
    await push(id);
  }

  async function onSaveAll() {
    for (const change of dirtyChanges) {
      await push(change.id);
    }
  }

  function onDiscard(id: string) {
    cancel(id);
  }

  function onDiscardAll() {
    for (const change of dirtyChanges) {
      cancel(change.id);
    }
  }

  return { isDarkMode, confirmOpen, setConfirmOpen, changes, onSave, onSaveAll, onDiscard, onDiscardAll };
};
