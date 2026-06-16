import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useFsTheme } from '../fs-theme';
import React from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';


export interface OwnerState {
  isDarkMode: boolean;
  labels: string[];
  labelOptions: string[];
  isDirty: boolean;
  onChangeLabels: (values: string[]) => void;
  onSave: () => Promise<void>;
}


export const useOwnerState = (props: FsDirentLabelsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { dirent } = props;
  const { updateDirentLabels, selectOptions } = useFsDirent();
  const init = (dirent.props?.labels ?? []).map(l => l.key)
  const [labels, setLabels] = React.useState(init);


  function onChangeLabels(values: string[]) {
    setLabels(values);
  }

  async function onSave() {
    await updateDirentLabels(dirent.id, labels.map(key => ({ key })));
  }

  return {
    isDarkMode,
    labels: labels,
    labelOptions: selectOptions.labels,
    isDirty: JSON.stringify(labels) !== JSON.stringify(init),
    onChangeLabels,
    onSave,
  };
};
