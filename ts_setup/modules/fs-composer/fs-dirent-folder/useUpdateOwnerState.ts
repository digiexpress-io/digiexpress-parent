import React from 'react';
import { FsDirent, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: FsDirent.Folder | undefined;
  location: string;
  name: string;
  onChangeName: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<FsDirent.Folder>(props.direntId);

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
