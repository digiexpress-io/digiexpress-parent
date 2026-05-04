import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


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
  const { getDirent } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const flowTaskProps = dirent?.type === 'FLOW_TASK' ? dirent.props as Fs.FlowTaskProps : undefined;

  const [taskName, setTaskName] = React.useState(flowTaskProps?.taskName ?? dirent?.name ?? '');
  const [taskValue, setTaskValue] = React.useState(flowTaskProps?.taskValue ?? '');

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
