import React from 'react';
import { FsDirent, FsDirentConfigOption, LinkDirentProps, collectLocales, mockFsData, useFsDirentProps, useFsNav } from '@dxs-ts/fs-api';


const locales = collectLocales(mockFsData);

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: (FsDirent & LinkDirentProps) | undefined;
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
  const { getDirent } = useFsDirentProps();

  const dirent = getDirent(props.direntId) as (FsDirent & LinkDirentProps) | undefined;

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
