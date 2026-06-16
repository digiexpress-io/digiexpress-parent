import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  technicalName: string;
  formName: string;
  onChangeTechnicalName: (value: string) => void;
  onChangeFormName: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const dialobProps = dirent?.type === 'DIALOB_FORM' ? dirent.props as Fs.DialobProps : undefined;

  const [technicalName, setTechnicalName] = React.useState(dialobProps?.formTechnicalId ?? dirent?.id ?? '');
  const [formName, setFormName] = React.useState(dialobProps?.formName ?? dirent?.name ?? '');

  function onChangeTechnicalName(value: string) {
    setTechnicalName(value);
  }
  function onChangeFormName(value: string) {
    setFormName(value);
  }

  return ({
    isDarkMode,
    assetPath: activeTabPath,
    dirent,
    technicalName,
    formName,
    onChangeTechnicalName,
    onChangeFormName,
  });
};
