import React from 'react';
import { FsDirent, FolderDirentProps, useFsDirentProps, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: (FsDirent & { type: 'folder' }) | undefined;
  location: string;
  name: string;
  onChangeName: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirentProps();

  const dirent = getDirent(props.direntId) as (FsDirent & { type: 'folder' }) | undefined;

  const [name, setName] = React.useState(dirent?.name ?? '');

  function onChangeName(value: string) {
    setName(value);
  }

  return ({
    isDarkMode,
    dirent,
    location: dirent?.name ?? '',
    name,
    onChangeName,
  });
};
