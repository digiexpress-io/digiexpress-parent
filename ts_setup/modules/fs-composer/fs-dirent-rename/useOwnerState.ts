import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentRenameProps } from './FsDirentRenameProps';


export interface OwnerState {
  name: string;
  isDirty: boolean;
  onChangeName: (value: string) => void;
  onSave: () => Promise<void>;
}


function getDisplayName(dirent: Fs.DirentBase | undefined): string {
  if (!dirent) {
    return '';
  }
  if (dirent.type === 'ARTICLE') {
    const parts = dirent.fullPath.split('/');
    return parts[parts.length - 2] ?? dirent.name;
  }
  return dirent.name;
}

export const useOwnerState = (props: FsDirentRenameProps): OwnerState => {
  const { dirent } = props;
  const { updateDirentName } = useFsDirent();
  const init = getDisplayName(dirent);
  const [name, setName] = React.useState(init);

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
