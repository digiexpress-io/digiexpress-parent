import React from 'react';
import { ArticleEntry, FsDirentConfigOption, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: ArticleEntry | undefined;
  name: string;
  orderNumber: string;
  description: string;
  configOptions: FsDirentConfigOption[];
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

  const dirent = getDirent<ArticleEntry>(props.direntId);

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [orderNumber, setOrderNumber] = React.useState(dirent?.orderNumber != null ? String(dirent.orderNumber) : '');
  const [description, setDescription] = React.useState(dirent?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<FsDirentConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirentConfigOption[]
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
    setConfigOptions(value as FsDirentConfigOption[]);
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
