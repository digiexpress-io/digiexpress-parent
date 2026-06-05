import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsu,
  FsuCreateChange,
} from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface TextFields {
  name: string;
  orderNumber: string;
  assetDescription: string;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  parentArticle: Fs.DirentBase | undefined;
  parentArticlePath: string | undefined;
  isChanged: boolean;
  isExpanded: boolean;
  name: string;
  orderNumber: string;
  assetDescription: string;
  labels: string[];
  configOptions: Fs.ConfigOption[];
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onBlurName: () => void;
  onBlurOrderNumber: () => void;
  onBlurDescription: () => void;
  onToggleExpanded: () => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
  parentId: string | undefined;
  order: number;
  assetDescription: string;
  labels: string[];
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  authOnly: boolean;
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get name() { return this._current.name; }
  get orderNumber() { return String(this._current.order); }
  get assetDescription() { return this._current.assetDescription; }
  get labels() { return this._current.labels; }
  get configOptions() { return this._current.configOptions; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        name: this._current.name,
        parentId: this._current.parentId,
        order: this._current.order,
        assetDescription: this._current.assetDescription,
        labels: this._current.labels,
        devMode: this._current.devMode,
        authOnly: this._current.authOnly,
      }
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
  withOrder(order: string): _CreateState {
    return new _CreateState({ ...this._current, order: parseInt(order) || 0 }, this._origin);
  }
  withDescription(assetDescription: string): _CreateState {
    return new _CreateState({ ...this._current, assetDescription }, this._origin);
  }
  withLabels(labels: string[]): _CreateState {
    return new _CreateState({ ...this._current, labels }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _CreateState {
    return new _CreateState({
      ...this._current,
      configOptions,
      devMode: configOptions.includes('DEV_MODE'),
      authOnly: configOptions.includes('AUTH_ONLY_MODE'),
    }, this._origin);
  }
}

const _initFields: TextFields = { name: '', orderNumber: '0', assetDescription: '' };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { pushCreate } = useFsu();
  const { openAsset, activeTabPath, openTabs, activeTabIndex } = useFsNav();

  const activeTab = openTabs[activeTabIndex];
  const parentFolder = activeTab?.type === 'create' ? activeTab.parentFolder : undefined;
  const parentArticle = parentFolder?.type === 'ARTICLE' ? parentFolder : undefined;
  const parentArticlePath = parentArticle ? activeTabPath : undefined;
  const parentId = parentArticle?.id;

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [fields, setFields] = React.useState<TextFields>(_initFields);
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState({
    bodyType: 'ARTICLE',
    name: '',
    parentId,
    order: 0,
    assetDescription: '',
    labels: [],
    configOptions: [],
    devMode: false,
    authOnly: false,
  }));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const isChangesPresent = state.isChanged
    || fields.name !== state.name
    || fields.orderNumber !== state.orderNumber
    || fields.assetDescription !== state.assetDescription;

  function onChangeName(value: string) {
    setFields(prev => ({ ...prev, name: value }));
  }

  function onChangeOrderNumber(value: string) {
    setFields(prev => ({ ...prev, orderNumber: value }));
  }

  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, assetDescription: value }));
  }

  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }

  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
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
    setState(prev => prev.withDescription(fields.assetDescription));
  }

  async function onSave() {
    try {
      const dirent = await pushCreate(state);
      openAsset(dirent);
    } catch {
      // error snackbar already shown by handlePushCreate
    }
  }

  function onCancel() {
    setFields(_initFields);
    setStateRaw(new _CreateState({
      bodyType: 'ARTICLE',
      name: '',
      parentId,
      order: 0,
      assetDescription: '',
      labels: [],
      configOptions: [],
      devMode: false,
      authOnly: false,
    }));
  }

  return ({
    isDarkMode,
    parentArticle,
    parentArticlePath,
    isChanged: isChangesPresent,
    isExpanded,
    name: fields.name,
    orderNumber: fields.orderNumber,
    assetDescription: fields.assetDescription,
    labels: state.labels,
    configOptions: state.configOptions,
    onChangeName,
    onBlurName,
    onChangeOrderNumber,
    onBlurOrderNumber,
    onChangeDescription,
    onBlurDescription,
    onChangeLabels,
    onChangeConfigOptions,
    onToggleExpanded,
    onSave,
    onCancel,
  });
};
