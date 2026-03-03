import { useFs } from '@dxs-ts/fs-api';
import React, { Dispatch, SetStateAction } from 'react';
import { FsChangesProps } from './FsChangesProps';
import { FsColors } from '../fs-theme';




export interface OwnerState {
  isDarkMode: boolean;
  confirmOpen: boolean;
  setConfirmOpen: Dispatch<SetStateAction<boolean>>;
  getStatusColor: (status: string, isDarkMode: boolean) => string;
}

export const useOwnerState = (_props: FsChangesProps): OwnerState => {
  const { isDarkMode } = useFs();
  const [confirmOpen, setConfirmOpen] = React.useState(false);

  return ({ isDarkMode, confirmOpen, setConfirmOpen, getStatusColor });
}

function getStatusColor(status: string, isDarkMode: boolean) {
  switch (status) {
    case 'deleted':
      return isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight;
    case 'new':
      return FsColors.semantic.success;
    case 'modified':
      return isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight;
    default:
      return isDarkMode ? FsColors.dark.text : FsColors.light.text;
  }
};