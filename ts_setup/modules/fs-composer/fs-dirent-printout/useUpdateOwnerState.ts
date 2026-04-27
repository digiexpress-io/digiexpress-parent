import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Dirent | undefined;
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
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const printout = dirent?.type === 'PRINTOUT' ? dirent : undefined;
  const locales = Object.keys(printout?.intlValues ?? {});

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [printoutServiceName, setPrintoutServiceName] = React.useState(printout?.printoutServiceName ?? '');
  const [orchestratorName, setOrchestratorName] = React.useState(printout?.orchestratorName ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(printout?.intlValues ?? {});

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
