import { useFs } from '@dxs-ts/fs-api';
import React, { Dispatch, SetStateAction } from 'react';
import { FsChangesProps } from './FsChangesProps';




export interface OwnerState {
  isDarkMode: boolean;
  confirmOpen: boolean;
  setConfirmOpen: Dispatch<SetStateAction<boolean>>
}


export const useOwnerState = (_props: FsChangesProps): OwnerState => {
  const { isDarkMode } = useFs();
  const [confirmOpen, setConfirmOpen] = React.useState(false);


  return ({ isDarkMode, confirmOpen, setConfirmOpen });
}