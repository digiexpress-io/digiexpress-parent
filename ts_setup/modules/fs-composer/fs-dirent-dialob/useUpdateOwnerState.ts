import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Dialob | undefined;
  technicalName: string;
  formName: string;
  description: string;
  isExpanded: boolean;
  onChangeTechnicalName: (value: string) => void;
  onChangeFormName: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<Fs.Dialob>(props.direntId);

  const [technicalName, setTechnicalName] = React.useState(dirent?.formTechnicalId ?? '');
  const [formName, setFormName] = React.useState(dirent?.formName ?? '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeTechnicalName(value: string) {
    setTechnicalName(value);
  }

  function onChangeFormName(value: string) {
    setFormName(value);
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
    technicalName,
    formName,
    description,
    isExpanded,
    onChangeTechnicalName,
    onChangeFormName,
    onChangeDescription,
    onToggleExpanded,
  });
};
