import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Page | undefined;
  localeCode: string;
  articleId: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  onChangeLocaleCode: (value: string) => void;
  onChangeArticleId: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<Fs.Page>(props.direntId);

  const [localeCode, setLocaleCode] = React.useState(dirent?.localeCode ?? '');
  const [articleId, setArticleId] = React.useState(dirent?.articleId ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.configOptions ?? []) as Fs.ConfigOption[]
  );

  function onChangeLocaleCode(value: string) {
    setLocaleCode(value);
  }

  function onChangeArticleId(value: string) {
    setArticleId(value);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  return ({
    isDarkMode,
    dirent,
    localeCode,
    articleId,
    description,
    configOptions,
    onChangeLocaleCode,
    onChangeArticleId,
    onChangeDescription,
    onChangeConfigOptions,
  });
};
