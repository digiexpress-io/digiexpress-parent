import React from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentCopyProps } from './FsDirentCopyProps';


export interface OwnerState {
  name: string;
  isDirty: boolean;
  onChangeName: (value: string) => void;
  onSave: () => Promise<void>;
}

export const useOwnerState = (props: FsDirentCopyProps): OwnerState => {
  const { dirent } = props;
  const { copyDirent } = useFsDirent();
  const [name, setName] = React.useState('');

  function onChangeName(value: string) {
    setName(value);
  }

  async function onSave() {
    await copyDirent(dirent.id, name);
  }

  return {
    name,
    isDirty: name.trim().length > 0,
    onChangeName,
    onSave,
  };
};
