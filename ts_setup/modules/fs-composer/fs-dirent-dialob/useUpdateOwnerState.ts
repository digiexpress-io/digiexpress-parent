import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
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
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const dialobProps = dirent?.type === 'DIALOB_FORM' ? dirent.props as Fs.DialobProps : undefined;

  const [technicalName, setTechnicalName] = React.useState(dialobProps?.formTechnicalId ?? dirent?.id ?? '');
  const [formName, setFormName] = React.useState(dialobProps?.formName ?? dirent?.name ?? '');
  const [description, setDescription] = React.useState(dirent?.props?.description ?? '');
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
