import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  isExpanded: boolean;
  name: string;
  tagLabels: string[];
  onChangeName: (value: string) => void;
  onBlurName: () => void;
  onChangeLabels: (value: string[]) => void;
  onToggleExpanded: () => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
  tagLabels: string[];
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
  get tagLabels() { return this._current.tagLabels; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        name: this._current.name || undefined,
        tagLabels: this._current.tagLabels,
      },
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
  withTagLabels(tagLabels: string[]): _CreateState {
    return new _CreateState({ ...this._current, tagLabels }, this._origin);
  }
}

const _initProps: _CreateStateProps = { bodyType: 'FLOW_TASK', name: '', tagLabels: [] };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [name, setName] = React.useState('');
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const isChangesPresent = state.isChanged || name !== state.name;

  function onChangeName(value: string) {
    setName(value);
  }

  function onBlurName() {
    setState(prev => prev.withName(name));
  }

  function onChangeLabels(value: string[]) {
    setState(prev => prev.withTagLabels(value));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
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
    setName('');
    setIsExpanded(false);
    setStateRaw(new _CreateState(_initProps));
  }

  return ({
    isDarkMode,
    isChanged: isChangesPresent,
    isExpanded,
    name,
    tagLabels: state.tagLabels,
    onChangeName,
    onBlurName,
    onChangeLabels,
    onToggleExpanded,
    onSave,
    onCancel,
  });
};
