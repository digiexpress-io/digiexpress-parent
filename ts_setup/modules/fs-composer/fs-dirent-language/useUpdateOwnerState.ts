import React from 'react';
import { FsDirentConfigOption, LanguageEntry, useFsDirentProps, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: LanguageEntry | undefined;
  name: string;
  localeCode: string;
  description: string;
  configOptions: FsDirentConfigOption[];
  onChangeName: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirentProps();

  const dirent = getDirent(props.direntId) as LanguageEntry | undefined;

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [localeCode, setLocaleCode] = React.useState(dirent?.localeCode ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<FsDirentConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirentConfigOption[]
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
    setConfigOptions(value as FsDirentConfigOption[]);
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
