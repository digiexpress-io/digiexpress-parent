import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


type _ChangeStateProps = {
  localeId: string;
  bodyType: Fs.BodyType;
  value: string;
  enabled: boolean;
  disabledMode: boolean;
  description: string;
  configOptions: Fs.ConfigOption[];
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
  get description() { return this._current.description; }
  get configOptions() { return this._current.configOptions; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }

  withDescription(description: string): _ChangeState {
    return new _ChangeState({ ...this._current, description }, this._origin);
  }

  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({
      ...this._current,
      configOptions,
      disabledMode: configOptions.includes('DISABLED_MODE'),
      enabled: !configOptions.includes('DISABLED_MODE'),
    }, this._origin);
  }
}


export interface TextFields {
  description: string;
  configOptions: Fs.ConfigOption[];
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  localeCode: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  isChanged: boolean;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onBlurDescription: () => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const languageProps = dirent.props as Fs.LanguageProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    localeId: props.direntId,
    bodyType: dirent.type,
    value: languageProps.localeCode,
    enabled: !(languageProps.configOptions ?? []).includes('DISABLED_MODE'),
    disabledMode: (languageProps.configOptions ?? []).includes('DISABLED_MODE'),
    description: languageProps.description ?? '',
    configOptions: (languageProps.configOptions ?? []) as Fs.ConfigOption[],
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [fields, setFields] = React.useState<TextFields>({
    description: languageProps.description ?? '',
    configOptions: (languageProps.configOptions ?? []) as Fs.ConfigOption[],
  });

  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, description: value }));
  }

  function onChangeConfigOptions(value: string[]) {
    const opts = value as Fs.ConfigOption[];
    setFields(prev => ({ ...prev, configOptions: opts }));
    setState(prev => prev.withConfigOptions(opts));
  }

  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.description));
  }

  function onCancel() {
    setFields({
      description: languageProps.description ?? '',
      configOptions: (languageProps.configOptions ?? []) as Fs.ConfigOption[],
    });
    cancel(props.direntId);
  }

  const changes = state.isChanged
    || fields.description !== state.description
    || JSON.stringify(fields.configOptions) !== JSON.stringify(state.configOptions);

  return ({
    isDarkMode,
    dirent,
    id: state.id,
    localeCode: languageProps.localeCode,
    description: fields.description,
    configOptions: fields.configOptions,
    isChanged: changes,
    onChangeDescription,
    onChangeConfigOptions,
    onBlurDescription,
    onCancel,
  });
};
