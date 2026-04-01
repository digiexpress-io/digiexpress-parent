import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Flow | undefined;
  name: string;
  onChangeName: (value: string) => void;
  content: string;
  onChangeContent: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<Fs.Flow>(props.direntId);

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [content, setContent] = React.useState(dirent?.content ?? '');

  function onChangeName(value: string) {
    setName(value);
  }

  function onChangeContent(value: string) {
    setContent(value);
  }

  return ({
    isDarkMode,
    dirent,
    name,
    onChangeName,
    content,
    onChangeContent,
  });
};
