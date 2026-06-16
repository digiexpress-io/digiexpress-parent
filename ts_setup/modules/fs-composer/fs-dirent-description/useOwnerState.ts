import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentDescriptionProps } from './FsDirentDescriptionProps';
import { useFsTheme } from '../fs-theme';
import React from 'react';


export interface OwnerState {
  isDarkMode: boolean;
  description: string;
  isDirty: boolean;
  onChangeDescription: (value: string) => void;
  onSave: () => Promise<void>;
}


export const useOwnerState = (props: FsDirentDescriptionProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { dirent } = props;
  const { updateDirentDescription } = useFsDirent();
  const init = dirent.props?.assetDescription ?? '';
  const [description, setDesc] = React.useState(init);

  function onChangeDescription(value: string) {
    setDesc(value);
  }

  async function onSave() {
    await updateDirentDescription(dirent.id, description);
  }

  return {
    isDarkMode,
    description,
    isDirty: description !== init,
    onChangeDescription,
    onSave,
  };
};
