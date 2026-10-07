import React from 'react';
import { Fs, FsuCreateChange, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { createWidget } from '../fs-factory';


export interface CreateOwnerState {
  isDirty: boolean;
  parentId: string;
  name: string;
  orderNumber: string;
  configOptions: Fs.ConfigOption[];
  onChangeParentId: (value: string) => void;
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
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get parentId() { return this._current.parentId; }
  get name() { return this._current.name; }
  get orderNumber() { return String(this._current.order); }
  get configOptions() { return this._current.configOptions; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        name: c.name,
        parentId: c.parentId,
        order: c.order,
        devMode: c.configOptions.includes('DEV_MODE') || undefined,
        authOnly: c.configOptions.includes('AUTH_ONLY_MODE') || undefined,
      }
    };
  }

  withParentId(parentId: string | undefined): _CreateState {
    return new _CreateState({ ...this._current, parentId }, this._origin);
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
  const { openTabs, activeTabIndex, openAsset } = useFsNav();

  const activeTab = openTabs[activeTabIndex];
  const parentFolder = activeTab?.type === 'create' ? activeTab.parentFolder : undefined;
  const initialParentId = parentFolder?.type === 'ARTICLE' ? parentFolder.id : undefined;

  const _initProps: _CreateStateProps = {
    bodyType: 'ARTICLE',
    name: '',
    parentId: initialParentId,
    order: 0,
    configOptions: [],
  }
  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_initProps));
  const isChangesPresent = state.isDirty;

  function onChangeParentId(value: string) {
    setState(prev => prev.withParentId(value || undefined));
  }
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
    isDirty: isChangesPresent,
    parentId: state.parentId ?? '',
    name: state.name,
    orderNumber: state.orderNumber,
    configOptions: state.configOptions,
    onChangeParentId,
    onChangeName,
    onChangeOrderNumber,
    onChangeConfigOptions,
    onSave,
  });
};
