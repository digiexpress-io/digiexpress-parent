import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Dialob | undefined;
  technicalName: string;
  formName: string;
  onChangeTechnicalName: (value: string) => void;
  onChangeFormName: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<Fs.Dialob>(props.direntId);

  const [technicalName, setTechnicalName] = React.useState(dirent?.formTechnicalId ?? '');
  const [formName, setFormName] = React.useState(dirent?.formName ?? '');

  function onChangeTechnicalName(value: string) {
    setTechnicalName(value);
  }

  function onChangeFormName(value: string) {
    setFormName(value);
  }

  return ({
    isDarkMode,
    dirent,
    technicalName,
    formName,
    onChangeTechnicalName,
    onChangeFormName,
  });
};
