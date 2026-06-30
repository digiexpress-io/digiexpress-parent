import React from 'react';
import { Fs, FsuCreateChange, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { createWidget } from '../fs-factory';


export interface CreateOwnerState {
  parentArticle: Fs.DirentBase | undefined;
  parentArticlePath: string | undefined;
  isDirty: boolean;
  name: string;
  orderNumber: string;
  configOptions: Fs.ConfigOption[];
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
  parentId: string | undefined;
  order: number;
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
  get configOptions() { return this._current.configOptions; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        name: this._current.name,
        parentId: this._current.parentId,
        order: this._current.order,
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
  withConfigOptions(value: string[]): _CreateState {
    const widget = createWidget({ type: 'ARTICLE' });
    return new _CreateState({ ...this._current, configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)) }, this._origin);
  }
}


export const useCreateOwnerState = (): CreateOwnerState => {
  const { createDirent } = useFsDirent();
  const { activeTabPath, openTabs, activeTabIndex, openAsset } = useFsNav();

  const activeTab = openTabs[activeTabIndex];
  const parentFolder = activeTab?.type === 'create' ? activeTab.parentFolder : undefined;
  const parentArticle = parentFolder?.type === 'ARTICLE' ? parentFolder : undefined;
  const parentArticlePath = parentArticle ? activeTabPath : undefined;
  const parentId = parentArticle?.id;


  const _init: _CreateStateProps = {
    bodyType: 'ARTICLE',
    name: '',
    parentId,
    order: 0,
    configOptions: [],
    devMode: false,
    authOnly: false,
  }
  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));
  const isChangesPresent = state.isDirty;


  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }
  function onChangeOrderNumber(value: string) {
    setState(prev => prev.withOrder(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value));
  }

  async function onSave() {
    const newDirent = await createDirent(state);
    openAsset(newDirent);
  }

  return ({
    parentArticle,
    parentArticlePath,
    isDirty: isChangesPresent,
    name: state.name,
    orderNumber: state.orderNumber,
    configOptions: state.configOptions,
    onChangeName,
    onChangeOrderNumber,
    onChangeConfigOptions,
    onSave,
  });
};
