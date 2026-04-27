import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  name: string;
  localeCode: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  onChangeName: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const languageProps = dirent?.type === 'LOCALE' ? dirent.props as Fs.LanguageProps : undefined;

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [localeCode, setLocaleCode] = React.useState(languageProps?.localeCode ?? '');
  const [description, setDescription] = React.useState(dirent?.props?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );

  function onChangeName(value: string) {
    setName(value);
  }

  function onChangeLocaleCode(value: string) {
    setLocaleCode(value);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  return ({
    isDarkMode,
    dirent,
    name,
    localeCode,
    description,
    configOptions,
    onChangeName,
    onChangeLocaleCode,
    onChangeDescription,
    onChangeConfigOptions,
  });
};
