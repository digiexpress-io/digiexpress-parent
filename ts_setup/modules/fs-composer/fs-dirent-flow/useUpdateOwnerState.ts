import React from 'react';
import { useFsTheme } from '../fs-theme';
import { createWidget } from '../fs-factory';
import {
  Fs,
  useFsDirent,
  FsuChange,
  useFsuChange,
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isDirty: boolean;
  content: string;
  configOptions: Fs.ConfigOption[];
  onChangeContent: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

type _ChangeStateProps = {
  flowId: string;
  bodyType: Fs.BodyType;
  flowValue: string;
  configOptions: Fs.ConfigOption[];
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
  get configOptions() { return this._current.configOptions; }
  get flowValue() { return this._current.flowValue; }
  get isDirty(): boolean {
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
  withConfigOptions(value: string[]): _ChangeState {
    const widget = createWidget({ type: 'FLOW' });
    return new _ChangeState({ ...this._current, configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)) }, this._origin);
  }

}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const dirent = getDirent(props.direntId);
  const flowProps = dirent?.props as Fs.FlowProps | undefined;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    flowId: props.direntId,
    bodyType: flowProps!.type,
    flowValue: flowProps?.content ?? '',
    configOptions: (flowProps?.configOptions ?? []) as Fs.ConfigOption[],
  }));

  function onChangeContent(value: string) {
    setState(prev => prev.withFlowValue(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isDirty: state.isDirty,
    content: state.flowValue,
    configOptions: state.configOptions,
    onChangeContent,
    onChangeConfigOptions,
  };
};
