import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, FsuCreateChange } from '@dxs-ts/fs-api';

export interface TextFields {
  locale: string;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  locale: string;
  isChanged: boolean;
  onChangeLocale: (value: string) => void;
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

const _init: _CreateStateProps = { bodyType: 'LOCALE', locale: '' };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));


  const isChangesPresent = state.isChanged;

  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }

  return ({
    locale: state.locale,
    isDarkMode,
    isChanged: isChangesPresent,
    onChangeLocale
  });
};