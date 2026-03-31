import React from 'react';
import { FsDirent, useFsDirent, useFsNav } from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: FsDirent.Phone | undefined;
  locales: string[];
  phoneValue: string;
  intlValues: Record<string, string>;
  configOptions: FsDirent.ConfigOption[];
  isExpanded: boolean;
  onChangePhoneValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent<FsDirent.Phone>(props.direntId);
  const locales = selectOptions.languages;

  const [phoneValue, setPhoneValue] = React.useState(dirent?.phoneValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<FsDirent.ConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirent.ConfigOption[]
  );
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangePhoneValue(value: string) {
    setPhoneValue(value);
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
    phoneValue,
    intlValues,
    configOptions,
    isExpanded,
    onChangePhoneValue,
    onChangeIntlValue,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
