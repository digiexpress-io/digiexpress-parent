import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  isDarkMode: boolean;
  name: string;
  desc: string;
  isChanged: boolean;
  onChangeName: (v: string) => void;
  onBlurName: () => void;
  onChangeDesc: (v: string) => void;
  onBlurDesc: () => void;
  onSave: () => Promise<void>;
  onCancel: () => void;
}

type TextFields = {
  name: string;
  desc: string;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
  desc: string;
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
  get desc() {
    return this._current.desc;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        name: c.name || undefined,
        desc: c.desc || undefined,
        nodes: [],
      },
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
  withDesc(desc: string): _CreateState {
    return new _CreateState({ ...this._current, desc }, this._origin);
  }
  }

const _initProps: _CreateStateProps = { bodyType: 'DECISION_TABLE', name: '', desc: '' };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
    const { pushCreate } = useFsu();
    const { openAsset } = useFsNav();

    const [fields, setFields] = React.useState<TextFields>({ name: _initProps.name, desc: _initProps.desc });
    const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

    function setState(updater: (prev: _CreateState) => _CreateState) {
      setStateRaw(prev => updater(prev));
    }

    const onChangeName = (v: string) => setFields(prev => ({ ...prev, name: v }));
    const onBlurName = () => setState(prev => prev.withName(fields.name));
    const onChangeDesc = (v: string) => setFields(prev => ({ ...prev, desc: v }));
    const onBlurDesc = () => setState(prev => prev.withDesc(fields.desc));

    const onSave = async () => {
      const dirent = await pushCreate(state);
      openAsset(dirent);
    };

    const onCancel = () => {
      setFields({ name: _initProps.name, desc: _initProps.desc });
      setStateRaw(new _CreateState(_initProps));
    };

    return {
      isDarkMode,
      name: fields.name,
      desc: fields.desc,
      isChanged: state.isChanged,
      onChangeName,
      onBlurName,
      onChangeDesc,
      onBlurDesc,
      onSave,
      onCancel,
    };
  };
