import React from 'react';
import { FsDirent, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: FsDirent.Article | undefined;
  name: string;
  orderNumber: string;
  description: string;
  configOptions: FsDirent.ConfigOption[];
  labels: string;
  comments: string;
  isExpanded: boolean;
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLabels: (value: string) => void;
  onChangeComments: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<FsDirent.Article>(props.direntId);

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [orderNumber, setOrderNumber] = React.useState(dirent?.orderNumber != null ? String(dirent.orderNumber) : '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<FsDirent.ConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirent.ConfigOption[]
  );
  const [labels, setLabels] = React.useState(
    (dirent?.labels ?? []).map(l => l.value).join(', ')
  );
  const [comments, setComments] = React.useState(
    (dirent?.comments ?? []).map(c => c.comment).join('\n')
  );
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeName(value: string) {
    setName(value);
  }

  function onChangeOrderNumber(value: string) {
    setOrderNumber(value);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as FsDirent.ConfigOption[]);
  }

  function onChangeLabels(value: string) {
    setLabels(value);
  }

  function onChangeComments(value: string) {
    setComments(value);
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({
    isDarkMode,
    dirent,
    name,
    orderNumber,
    description,
    configOptions,
    labels,
    comments,
    isExpanded,
    onChangeName,
    onChangeOrderNumber,
    onChangeDescription,
    onChangeConfigOptions,
    onChangeLabels,
    onChangeComments,
    onToggleExpanded,
  });
};
