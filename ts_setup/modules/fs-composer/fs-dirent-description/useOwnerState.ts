import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentDescriptionProps } from './FsDirentDescriptionProps';
import React from 'react';


export interface OwnerState {
  description: string;
  isDirty: boolean;
  onChangeDescription: (value: string) => void;
  onSave: () => Promise<void>;
}


export const useOwnerState = (props: FsDirentDescriptionProps): OwnerState => {
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
    description,
    isDirty: description !== init,
    onChangeDescription,
    onSave,
  };
};
