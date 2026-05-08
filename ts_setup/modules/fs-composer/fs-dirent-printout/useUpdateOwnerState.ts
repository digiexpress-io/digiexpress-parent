import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  locales: string[];
  name: string;
  printoutServiceName: string;
  orchestratorName: string;
  description: string;
  intlValues: Record<string, string>;
  onChangeName: (value: string) => void;
  onChangePrintoutServiceName: (value: string) => void;
  onChangeOrchestratorName: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const printoutProps = dirent?.type === 'PRINTOUT' ? dirent.props as Fs.PrintoutProps : undefined;
  const locales = Object.keys(printoutProps?.intlValues ?? {});

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [printoutServiceName, setPrintoutServiceName] = React.useState(printoutProps?.printoutServiceName ?? '');
  const [orchestratorName, setOrchestratorName] = React.useState(printoutProps?.orchestratorName ?? '');
  const [description, setDescription] = React.useState(dirent?.props?.description ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(printoutProps?.intlValues ?? {});

  function onChangeName(value: string) {
    setName(value);
  }

  function onChangePrintoutServiceName(value: string) {
    setPrintoutServiceName(value);
  }

  function onChangeOrchestratorName(value: string) {
    setOrchestratorName(value);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeIntlValue(locale: string, value: string) {
    setIntlValues(prev => ({ ...prev, [locale]: value }));
  }

  return ({
    isDarkMode,
    dirent,
    locales,
    name,
    printoutServiceName,
    orchestratorName,
    description,
    intlValues,
    onChangeName,
    onChangePrintoutServiceName,
    onChangeOrchestratorName,
    onChangeDescription,
    onChangeIntlValue,
  });
};
