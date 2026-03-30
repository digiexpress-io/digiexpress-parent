import React from 'react';
import { FsDirentConfigOption, PageEntry, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: PageEntry | undefined;
  localeCode: string;
  articleId: string;
  description: string;
  configOptions: FsDirentConfigOption[];
  onChangeLocaleCode: (value: string) => void;
  onChangeArticleId: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<PageEntry>(props.direntId);

  const [localeCode, setLocaleCode] = React.useState(dirent?.localeCode ?? '');
  const [articleId, setArticleId] = React.useState(dirent?.articleId ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<FsDirentConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirentConfigOption[]
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
    setConfigOptions(value as FsDirentConfigOption[]);
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
