import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  //phoneValue: string;
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

  const dirent = getDirent(props.direntId);
  const phoneProps = dirent?.type === 'ARTICLE_LINK' ? dirent.props as Fs.PhoneProps : undefined;
  const locales = selectOptions.languages;

  //const [phoneValue, setPhoneValue] = React.useState(phoneProps?.phoneValue ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(phoneProps?.intlValues ?? {});
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );
  const [description, setDescription] = React.useState(dirent?.props?.description ?? '');
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangePhoneValue(value: string) {
    //setPhoneValue(value);
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
    //phoneValue,
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
