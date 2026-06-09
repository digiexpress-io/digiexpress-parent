import React from 'react';
import { Fs, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() {
    return this._current.bodyType;
  }
  get name() {
    return this._current.name;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        name: this._current.name || undefined,
      },
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
}

const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_TEMPLATE',
  name: '',
};

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  name: string;
  onChangeName: (value: string) => void;
  onSave: () => void;
  onCancel: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }

  async function onSave() {
    try {
      const dirent = await pushCreate(state);
      openAsset(dirent);
    } catch {
      // error snackbar already shown by pushCreate
    }
  }

  function onCancel() {
    setState(new _CreateState(_init));
  }

  return ({
    isDarkMode,
    isChanged: state.isChanged,
    name: state.name,
    onChangeName,
    onSave,
    onCancel,
  });
};
