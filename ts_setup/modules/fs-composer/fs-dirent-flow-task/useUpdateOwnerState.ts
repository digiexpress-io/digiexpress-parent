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
  private _liveContent: React.MutableRefObject<string>;

  constructor(props: _ChangeStateProps, liveContent: React.MutableRefObject<string>, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
    this._liveContent = liveContent;
  }

  get id() { return this._current.flowTaskId; }
  get bodyType() { return this._current.bodyType; }
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
        flowTaskValue: this._liveContent.current,
        tagLabels: this._current.tagLabels,
      },
    };
  }

  withFlowTaskValue(flowTaskValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowTaskValue }, this._liveContent, this._origin);
  }
  withTagLabels(tagLabels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, tagLabels }, this._liveContent, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, fetchDirentBody } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const withChangeRef = React.useRef(withChange);
  withChangeRef.current = withChange;

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChangeRef.current(props.direntId, callback);

  const dirent = getDirent(props.direntId);
  const flowTaskProps = dirent?.type === 'FLOW_TASK' ? dirent.props as Fs.FlowTaskProps : undefined;

  const [isExpanded, setIsExpanded] = React.useState(false);

  const originalContentRef = React.useRef<string>('');
  const liveContentRef = React.useRef<string>('');
  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const [taskValue, setTaskValue] = React.useState('');

  const state = withNewChange(props.direntId, () => new _ChangeState(
    {
      flowTaskId: props.direntId,
      bodyType: dirent!.type,
      flowTaskValue: liveContentRef.current,
      tagLabels: (flowTaskProps?.labels ?? []).map(l => l.value),
    },
    liveContentRef,
  ));

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'FLOW_TASK').then((body) => {
      const wb = body as Fs.WrenchBody;
      const yaml = wb.services[props.direntId]?.ast?.value ?? '';
      originalContentRef.current = yaml;
      liveContentRef.current = yaml;
      setTaskValue(yaml);
      cancel(props.direntId);
    });
  }, [props.direntId]);

  function onChangeTaskValue(value: string) {
    liveContentRef.current = value;
    setTaskValue(value);
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      setState(prev => prev.withFlowTaskValue(value));
    }, 500);
  }

  function onChangeLabels(value: string[]) {
    setState(prev => prev.withTagLabels(value));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  function onCancel() {
    const original = originalContentRef.current;
    liveContentRef.current = original;
    setTaskValue(original);
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isChanged: state.isChanged,
    isExpanded,
    taskValue,
    tagLabels: state.tagLabels,
    onChangeTaskValue,
    onChangeLabels,
    onToggleExpanded,
    onCancel,
  };
};
