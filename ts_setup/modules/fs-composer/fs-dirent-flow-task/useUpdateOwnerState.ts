import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isChanged: boolean;
  isExpanded: boolean;
  taskValue: string;
  tagLabels: string[];
  onChangeTaskValue: (value: string) => void;
  onChangeLabels: (value: string[]) => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

type _ChangeStateProps = {
  flowTaskId: string;
  bodyType: Fs.BodyType;
  flowTaskValue: string;
  tagLabels: string[];
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.flowTaskId; }
  get bodyType() { return this._current.bodyType; }
  get flowTaskValue() { return this._current.flowTaskValue; }
  get tagLabels() { return this._current.tagLabels; }

  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this.id,
      changes: {
        flowTaskId: this.id,
        flowTaskValue: this._current.flowTaskValue,
        tagLabels: this._current.tagLabels,
      },
    };
  }

  withFlowTaskValue(flowTaskValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowTaskValue }, this._origin);
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
  const flowTaskProps = dirent?.props as Fs.FlowTaskProps | undefined;

  const [isExpanded, setIsExpanded] = React.useState(false);

  const state = withNewChange(props.direntId, () => new _ChangeState({
    flowTaskId: props.direntId,
    bodyType: flowTaskProps!.type,
    flowTaskValue: flowTaskProps?.taskValue ?? '',
    tagLabels: (flowTaskProps?.labels ?? []).map(l => l.value),
  }));

  function onChangeTaskValue(value: string) {
    setState(prev => prev.withFlowTaskValue(value));
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
    taskValue: state.flowTaskValue,
    tagLabels: state.tagLabels,
    onChangeTaskValue,
    onChangeLabels,
    onToggleExpanded,
    onCancel,
  };
};
