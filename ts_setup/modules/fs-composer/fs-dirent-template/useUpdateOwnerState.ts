import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Dirent | undefined;
  content: string;
  onChangeContent: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const articleTemplate = dirent?.type === 'ARTICLE_TEMPLATE' ? dirent : undefined;


  const [content, setContent] = React.useState(articleTemplate?.content ?? '');

  function onChangeContent(value: string) {
    setContent(value);
  }

  return ({
    isDarkMode,
    dirent,
    content,
    onChangeContent,
  });
};
