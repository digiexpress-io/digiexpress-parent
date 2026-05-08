import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  name: string;
  orderNumber: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  labels: string[];
  comments: string;
  isExpanded: boolean;
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeComments: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, getArticleName } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const displayName = getArticleName(props.direntId);
  const articleProps = dirent?.type === 'ARTICLE' ? dirent.props as Fs.ArticleProps : undefined;

  const [name, setName] = React.useState(displayName ?? '');
  const [orderNumber, setOrderNumber] = React.useState(articleProps?.orderNumber != undefined ? String(articleProps.orderNumber) : '');
  const [description, setDescription] = React.useState(dirent?.props?.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );

  const [labels, setLabels] = React.useState<string[]>(
    (dirent?.props?.labels ?? []).map(l => l.value)
  );
  const [comments, setComments] = React.useState(
    (dirent?.props?.comments ?? []).map(c => c.comment).join('\n')
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
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  function onChangeLabels(value: string[]) {
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
