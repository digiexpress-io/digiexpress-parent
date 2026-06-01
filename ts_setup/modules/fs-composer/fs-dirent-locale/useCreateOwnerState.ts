import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface TextFields {
  locale: string;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  locale: string;
  isChanged: boolean;
  onChangeLocale: (value: string) => void;
  onBlurLocale: () => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  locale: string;
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get locale() { return this._current.locale; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: { locale: this._current.locale },
    };
  }

  withLocale(locale: string): _CreateState {
    return new _CreateState({ ...this._current, locale }, this._origin);
  }
}

const _initFields: _CreateStateProps = { bodyType: 'LOCALE', locale: '' };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [fields, setFields] = React.useState<TextFields>(_initFields);
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initFields));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const isChangesPresent = state.isChanged || fields.locale !== state.locale;

  function onChangeLocale(value: string) {
    setFields(prev => ({ ...prev, locale: value }));
  }

  function onBlurLocale() {
    setState(prev => prev.withLocale(fields.locale));
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
    setStateRaw(new _CreateState(_initFields));
  }

  return ({
    isDarkMode,
    locale: fields.locale,
    isChanged: isChangesPresent,
    onChangeLocale,
    onBlurLocale,
    onSave,
    onCancel,
  });
};