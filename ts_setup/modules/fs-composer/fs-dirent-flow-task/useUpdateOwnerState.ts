import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  taskName: string;
  taskValue: string;
  onChangeTaskName: (value: string) => void;
  onChangeTaskValue: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, fetchDirentBody } = useFsDirent();

  const dirent = getDirent(props.direntId);

  const [taskName, setTaskName] = React.useState(dirent?.name ?? '');
  const [taskValue, setTaskValue] = React.useState('');

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'FLOW_TASK').then((body) => {
      const wb = body as Fs.WrenchBody;
      const yaml = wb.services[props.direntId]?.ast?.value ?? '';
      setTaskValue(yaml);
    });
  }, [props.direntId]);

  function onChangeTaskName(value: string) {
    setTaskName(value);
  }

  function onChangeTaskValue(value: string) {
    setTaskValue(value);
  }

  return ({
    isDarkMode,
    dirent,
    taskName,
    taskValue,
    onChangeTaskName,
    onChangeTaskValue,
  });
};
