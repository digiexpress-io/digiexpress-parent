import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  description: string;
  isExpanded: boolean;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const linkProps = dirent?.type === 'ARTICLE_LINK' ? dirent.props as Fs.LinkProps : undefined;
  const locales = selectOptions.languages;

  const [urlValue, setUrlValue] = React.useState(linkProps?.urlValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(linkProps?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );
  const [articles, setArticles] = React.useState<string[]>([]);
  const [description, setDescription] = React.useState(dirent?.props?.description ?? '');
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeUrlValue(value: string) {
    setUrlValue(value);
  }

  function onChangeIntlValue(locale: string, value: string) {
    setIntlValues(prev => ({ ...prev, [locale]: value }));
  }

  function onChangeArticles(value: string[]) {
    setArticles(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({
    isDarkMode,
    dirent,
    locales,
    urlValue,
    intlValues,
    articles,
    configOptions,
    description,
    isExpanded,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeArticles,
    onChangeConfigOptions,
    onChangeDescription,
    onToggleExpanded,
  });
};
