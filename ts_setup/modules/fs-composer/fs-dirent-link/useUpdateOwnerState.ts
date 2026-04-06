import React from 'react';
import {
  Fs,
  useFsDirent, useFsNav
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Link | undefined;
  locales: string[];
  urlValue: string;
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  description: string;
  isExpanded: boolean;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent<Fs.Link>(props.direntId);
  const locales = selectOptions.languages;

  const [urlValue, setUrlValue] = React.useState(dirent?.urlValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.configOptions ?? []) as Fs.ConfigOption[]
  );
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeUrlValue(value: string) {
    setUrlValue(value);
  }

  function onChangeIntlValue(locale: string, value: string) {
    setIntlValues(prev => ({ ...prev, [locale]: value }));
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
    configOptions,
    description,
    isExpanded,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeConfigOptions,
    onChangeDescription,
    onToggleExpanded,
  });
};
