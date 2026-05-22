import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


export interface CreateOwnerState {
  isDarkMode: boolean;
  activeTabPath: string;
  articleId: string;
  localeCode: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isExpanded: boolean;
  onChangeArticleId: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { selectOptions, getArticleName, getConfigOptionsForType } = useFsDirent();


  const [articleId, setArticleId] = React.useState('');
  const [localeCode, setLocaleCode] = React.useState('');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>([]);
  const [isExpanded, setIsExpanded] = React.useState(false);

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles.map(o => ({
    value: o.value,
    label: getArticleName(o.value) ?? o.label,
  }));
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages;
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

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({
    isDarkMode,
    activeTabPath,
    articleId,
    localeCode,
    configOptions,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    isExpanded,
    onChangeArticleId,
    onChangeLocaleCode,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
