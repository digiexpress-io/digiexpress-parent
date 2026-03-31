import React from 'react';
import { FsDirent, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: FsDirent.Language | undefined;
  name: string;
  localeCode: string;
  description: string;
  configOptions: FsDirent.ConfigOption[];
  onChangeName: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<FsDirent.Language>(props.direntId);

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [localeCode, setLocaleCode] = React.useState(dirent?.localeCode ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<FsDirent.ConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirent.ConfigOption[]
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
    setConfigOptions(value as FsDirent.ConfigOption[]);
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
