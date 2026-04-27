import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string;
  articleId: string;
  localeCode: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  onChangeArticleId: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();

  const pathToTopParent = props.pathToTopParent ?? '';

  const [articleId, setArticleId] = React.useState('');
  const [localeCode, setLocaleCode] = React.useState('');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>([]);

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles;
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.map(lang => ({ value: lang, label: lang }));
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');

  function onChangeArticleId(value: string) {
    setArticleId(value);
  }

  function onChangeLocaleCode(value: string) {
    setLocaleCode(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  return ({
    isDarkMode,
    parentFolder: props.parentFolder,
    pathToTopParent,
    articleId,
    localeCode,
    configOptions,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    onChangeArticleId,
    onChangeLocaleCode,
    onChangeConfigOptions,
  });
};
