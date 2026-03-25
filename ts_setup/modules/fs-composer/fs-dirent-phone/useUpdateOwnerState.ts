import React from 'react';
import { FsDirent, FsDirentConfigOption, PhoneDirentProps, collectLocales, mockFsData, useFsDirentProps, useFsNav } from '@dxs-ts/fs-api';


const locales = collectLocales(mockFsData);

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: (FsDirent & PhoneDirentProps) | undefined;
  locales: string[];
  phoneValue: string;
  intlValues: Record<string, string>;
  configOptions: FsDirentConfigOption[];
  isExpanded: boolean;
  onChangePhoneValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirentProps();

  const dirent = getDirent(props.direntId) as (FsDirent & PhoneDirentProps) | undefined;

  const [phoneValue, setPhoneValue] = React.useState(dirent?.phoneValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<FsDirentConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirentConfigOption[]
  );
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangePhoneValue(value: string) {
    setPhoneValue(value);
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
