import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange,
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isChanged: boolean;
  isExpanded: boolean;
  content: string;
  assetDescription: string;
  configOptions: Fs.ConfigOption[];
  tagLabels: string[];
  onChangeContent: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

type _ChangeStateProps = {
  flowId: string;
  bodyType: Fs.BodyType;
  flowValue: string;
  assetDescription: { text: string };
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
  tagLabels: string[];
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.flowId; }
  get bodyType() { return this._current.bodyType; }
  get assetDescription() { return this._current.assetDescription; }
  get configOptions() { return this._current.configOptions; }
  get flowValue() { return this._current.flowValue; }
  get tagLabels() { return this._current.tagLabels; }


  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this.id,
      changes: { ...this._current },
    };
  }

  withFlowValue(flowValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowValue }, this._origin);
  }

  withDescription(assetDescription: { text: string }): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }

  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions, devMode: configOptions.includes('DEV_MODE'), disabledMode: configOptions.includes('DISABLED_MODE') }, this._origin);
  }

  withTagLabels(tagLabels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, tagLabels }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const dirent = getDirent(props.direntId);
  const flowProps = dirent?.props as Fs.FlowProps | undefined;

  const [isExpanded, setIsExpanded] = React.useState(false);

  const state = withNewChange(props.direntId, () => new _ChangeState({
    flowId: props.direntId,
    bodyType: flowProps!.type,
    flowValue: flowProps?.content ?? '',
    assetDescription: { text: flowProps?.assetDescription ?? '' },
    configOptions: (flowProps?.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (flowProps?.configOptions ?? []).includes('DEV_MODE'),
    disabledMode: (flowProps?.configOptions ?? []).includes('DISABLED_MODE'),
    tagLabels: (flowProps?.labels ?? []).map(l => l.value),
  }));

  function onChangeContent(value: string) {
    setState(prev => prev.withFlowValue(value));
  }
  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription({ text: value }));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withTagLabels(value));
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }
  function onCancel() {
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isChanged: state.isChanged,
    isExpanded,
    content: state.flowValue,
    assetDescription: state.assetDescription?.text,
    configOptions: state.configOptions,
    tagLabels: state.tagLabels,
    onChangeContent,
    onChangeConfigOptions,
    onChangeLabels,
    onChangeDescription,
    onToggleExpanded,
    onCancel,
  };
};
