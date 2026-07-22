import React from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentRenameProps } from './FsDirentRenameProps';


export interface OwnerState {
  name: string;
  isDirty: boolean;
  onChangeName: (value: string) => void;
  onSave: () => Promise<void>;
}


export const useOwnerState = (props: FsDirentRenameProps): OwnerState => {
  const { dirent } = props;
  const { updateDirentName } = useFsDirent();
  const init = dirent?.name ?? '';
  const [name, setName] = React.useState(init);

  console.log(dirent.fullPath)

  function onChangeName(value: string) {
    setName(value);
  }

  async function onSave() { 
    await updateDirentName(dirent.id, name);
   }

  return {
    name,
    isDirty: name !== init && name.trim().length > 0,
    onChangeName,
    onSave,
  };
};
