import React from 'react';
import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';


type _ChangeStateProps = {
  localeId: string;
  bodyType: Fs.BodyType;
  value: string;
  enabled: boolean;
  configOptions: Fs.ConfigOption[];
  assetDescription: string | undefined;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.localeId; }
  get bodyType() { return this._current.bodyType; }
  get value() { return this._current.value; }
  get configOptions() { return this._current.configOptions; }
  get assetDescription() { return this._current.assetDescription; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({
      ...this._current,
      configOptions,
      enabled: !configOptions.includes('DISABLED_MODE'),
    }, this._origin);
  }
  withLocaleCode(value: string): _ChangeState {
    return new _ChangeState({ ...this._current, value }, this._origin);
  }
  withDescription(assetDescription: string | undefined): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }

}


export interface TextFields {
  assetDescription: string;
  configOptions: Fs.ConfigOption[];
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  id: string;
  localeCode: string;
  configOptions: Fs.ConfigOption[];
  isDirty: boolean;
  assetDescription: string | undefined;
  onChangeConfigOptions: (value: string[]) => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { getDirent } = useFsDirent();


  const dirent = getDirent(props.direntId)!;
  const languageProps = dirent.props as Fs.LanguageProps;

  const { state, update, cancel } = useFsuChange(props.direntId, () => new _ChangeState({
    localeId: props.direntId,
    bodyType: dirent.type,
    value: languageProps.localeCode,
    enabled: !(languageProps.configOptions ?? []).includes('DISABLED_MODE'),
    configOptions: (languageProps.configOptions ?? []) as Fs.ConfigOption[],
    assetDescription: languageProps.assetDescription,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback)


  function onChangeConfigOptions(value: string[]) {
    const opts = value as Fs.ConfigOption[];
    setState(prev => prev.withConfigOptions(opts));
  }
  function onCancel() {
    cancel();
  }

  const changes = state.isDirty;

  return ({
    isDarkMode,
    assetPath: activeTabPath,
    dirent,
    id: state.id,
    localeCode: state.value,
    configOptions: state.configOptions,
    isDirty: changes,
    assetDescription: state.assetDescription,
    onChangeConfigOptions,
    onCancel,
  });
};
