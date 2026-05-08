import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  content: string;
  onChangeContent: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const templateProps = dirent?.type === 'ARTICLE_TEMPLATE' ? dirent.props as Fs.TemplateProps : undefined;

  const [content, setContent] = React.useState(templateProps?.content ?? '');

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
