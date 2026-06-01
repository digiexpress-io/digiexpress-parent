import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface TextFields {
  name: string;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  name: string;
  onChangeName: (value: string) => void;
  onBlurName: () => void;
  onSave: () => void;
  onCancel: () => void;
}

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

const _initProps: _CreateStateProps = { bodyType: 'FLOW_TASK', name: '' };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
    const { pushCreate } = useFsu();
    const { openAsset } = useFsNav();

    const [fields, setFields] = React.useState<TextFields>({ name: _initProps.name });
    const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

    const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

    const isChangesPresent = state.isChanged || fields.name !== state.name;

    function onChangeName(value: string) {
      setFields(prev => ({ ...prev, name: value }));
    }

    function onBlurName() {
      setState(prev => prev.withName(fields.name));
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
      setFields({ name: _initProps.name });
      setStateRaw(new _CreateState(_initProps));
    }

    return ({
      isDarkMode,
      isChanged: isChangesPresent,
      name: fields.name,
      onChangeName,
      onBlurName,
      onSave,
      onCancel,
    });
  };
