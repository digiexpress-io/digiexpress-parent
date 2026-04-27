import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Dirent | undefined;
  articleId: string;
  localeCode: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  onChangeArticleId: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  content: string;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions, getConfigOptionsForType } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const page = dirent?.type === 'ARTICLE_PAGE' ? dirent : undefined;
  console.log('page dirent:', page);
  const [content, setContent] = React.useState(page?.content ?? '');
  const [articleId, setArticleId] = React.useState(page?.articleId ?? '');
  const [localeCode, setLocaleCode] = React.useState(page?.localeCode ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.configOptions ?? []) as Fs.ConfigOption[]
  );

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles;
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.map(lang => ({ value: lang, label: lang }));
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('page');

  function onChangeArticleId(value: string) {
    setArticleId(value);
  }

  function onChangeLocaleCode(value: string) {
    setLocaleCode(value);
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
    content,
    articleId,
    localeCode,
    description,
    configOptions,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    onChangeArticleId,
    onChangeLocaleCode,
    onChangeDescription,
    onChangeConfigOptions,
  });
};
