import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange
} from '@dxs-ts/fs-api';


export interface TextFields {
  name: string;
  orderNumber: string;
  assetDescription: string;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  isLoading: boolean;
  id: string;
  isChanged: boolean;
  name: string;
  orderNumber: string;
  configOptions: Fs.ConfigOption[];
  assetDescription: string;
  labels: string[];
  comments: string;
  isExpanded: boolean;
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeComments: (value: string) => void;
  onBlurOrderNumber: () => void;
  onBlurDescription: () => void;
  onBlurName: () => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

type _ChangeStateProps = {
  articleId: string;
  bodyType: Fs.BodyType;
  name: string;
  order: number;
  assetDescription: {
    text: string
  };
  labels: string[];
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  authOnly: boolean;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.articleId; }
  get name() { return this._current.name; }
  get orderNumber() { return String(this._current.order); }
  get assetDescription() { return this._current.assetDescription; }
  get labels() { return this._current.labels; }
  get configOptions() { return this._current.configOptions; }

  get bodyType() { return this._current.bodyType; }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }

  withName(name: string): _ChangeState {
    return new _ChangeState({ ...this._current, name }, this._origin);
  }
  withOrder(order: string): _ChangeState {
    return new _ChangeState({ ...this._current, order: parseInt(order) || 0 }, this._origin);
  }
  withDescription(assetDescription: { text: string }): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }
  withLabels(labels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({
      ...this._current,
      configOptions,
      devMode: configOptions.includes('DEV_MODE'),
      authOnly: configOptions.includes('AUTH_ONLY_MODE'),
    }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, getDirentName } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId);
  const articleProps = dirent?.type === 'ARTICLE' ? dirent.props as Fs.ArticleProps : undefined;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    articleId: props.direntId,
    bodyType: dirent!.type,
    name: getDirentName(props.direntId) ?? '',
    order: articleProps?.orderNumber ?? 0,
    assetDescription: { text: articleProps?.assetDescription ?? '' },
    labels: (articleProps?.labels ?? []).map(l => l.value),
    configOptions: (articleProps?.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (articleProps?.configOptions ?? []).includes('DEV_MODE'),
    authOnly: (articleProps?.configOptions ?? []).includes('AUTH_ONLY_MODE'),
  }));


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [comments, setComments] = React.useState((dirent?.props?.comments ?? []).map(c => c.comment).join('\n'));
  const [fields, setFields] = React.useState<TextFields>({
    name: getDirentName(props.direntId) ?? '',
    orderNumber: String(articleProps?.orderNumber ?? 0),
    assetDescription: articleProps?.assetDescription ?? '',
  });



  function onChangeName(value: string) {
    setFields(prev => ({ ...prev, name: value }));
  }
  function onChangeOrderNumber(value: string) {
    setFields(prev => ({ ...prev, orderNumber: value }));
  }
  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, assetDescription: value }));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }
  function onChangeComments(value: string) {
    setComments(value);
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }
  function onBlurName() {
    setState(prev => prev.withName(fields.name));
  }
  function onBlurOrderNumber() {
    setState(prev => prev.withOrder(fields.orderNumber));
  }
  function onBlurDescription() {
    setState(prev => prev.withDescription({ text: fields.assetDescription }));
  }

  function onCancel() {
    setFields({
      name: getDirentName(props.direntId) ?? '',
      orderNumber: String(articleProps?.orderNumber ?? 0),
      assetDescription: articleProps?.assetDescription ?? '',
    });
    cancel(props.direntId);
  }

  const changes = state.isChanged
    || fields.name !== state.name
    || fields.orderNumber !== state.orderNumber
    //|| fields.assetDescription !== state.assetDescription
    ;


  return ({
    isDarkMode,
    dirent,
    isLoading: !dirent,
    id: state.id,
    isChanged: changes,
    name: fields.name,
    orderNumber: fields.orderNumber,
    assetDescription: fields.assetDescription,
    labels: state.labels,
    configOptions: state.configOptions,
    comments,
    isExpanded,
    onChangeName,
    onBlurName,
    onChangeOrderNumber,
    onBlurOrderNumber,
    onChangeConfigOptions,
    onChangeDescription,
    onBlurDescription,
    onChangeLabels,
    onChangeComments,
    onToggleExpanded,
    onCancel,
  });
};
