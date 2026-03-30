import React from 'react';
import {
  FsDirentConfigOption, LinkEntry, mockFsData,
  useFsDirent, useFsNav, FsDirentData, mockFsDirentProperties
} from '@dxs-ts/fs-api';


const data = new FsDirentData(mockFsData, mockFsDirentProperties);

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: LinkEntry | undefined;
  locales: string[];
  urlValue: string;
  intlValues: Record<string, string>;
  configOptions: FsDirentConfigOption[];
  isExpanded: boolean;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<LinkEntry>(props.direntId);
  const locales = data.languages;

  const [urlValue, setUrlValue] = React.useState(dirent?.urlValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<FsDirentConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirentConfigOption[]
  );
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeUrlValue(value: string) {
    setUrlValue(value);
  }

  function onChangeIntlValue(locale: string, value: string) {
    setIntlValues(prev => ({ ...prev, [locale]: value }));
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as FsDirentConfigOption[]);
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
