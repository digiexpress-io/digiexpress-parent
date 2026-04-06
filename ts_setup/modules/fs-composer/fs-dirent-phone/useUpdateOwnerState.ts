import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Phone | undefined;
  locales: string[];
  phoneValue: string;
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  description: string;
  isExpanded: boolean;
  onChangePhoneValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent<Fs.Phone>(props.direntId);
  const locales = selectOptions.languages;

  const [phoneValue, setPhoneValue] = React.useState(dirent?.phoneValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.configOptions ?? []) as Fs.ConfigOption[]
  );
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangePhoneValue(value: string) {
    setPhoneValue(value);
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
    phoneValue,
    intlValues,
    configOptions,
    description,
    isExpanded,
    onChangePhoneValue,
    onChangeIntlValue,
    onChangeConfigOptions,
    onChangeDescription,
    onToggleExpanded,
  });
};
