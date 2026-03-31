import React from 'react';
import {
  FsDirent,
  useFsDirent, useFsNav
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: FsDirent.Link | undefined;
  locales: string[];
  urlValue: string;
  intlValues: Record<string, string>;
  configOptions: FsDirent.ConfigOption[];
  isExpanded: boolean;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent<FsDirent.Link>(props.direntId);
  const locales = selectOptions.languages;

  const [urlValue, setUrlValue] = React.useState(dirent?.urlValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<FsDirent.ConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirent.ConfigOption[]
  );
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeUrlValue(value: string) {
    setUrlValue(value);
  }

  function onChangeIntlValue(locale: string, value: string) {
    setIntlValues(prev => ({ ...prev, [locale]: value }));
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as FsDirent.ConfigOption[]);
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
    configOptions,
    isExpanded,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
