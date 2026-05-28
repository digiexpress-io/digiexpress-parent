import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange
} from '@dxs-ts/fs-api';

export interface TextFields {
  content: string;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isChanged: boolean;
  content: string;
  onChangeContent: (value: string) => void;
  onCancel: () => void;
}

type _ChangeStateProps = {
  flowId: string;
  bodyType: Fs.BodyType;
  flowValue: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;
  // Holds a pointer to the ref object — dereferenced at save time so getCurrentProps()
  // always returns the latest editor content regardless of when this instance was created.
  private _liveContent: React.MutableRefObject<string>;

  constructor(props: _ChangeStateProps, liveContent: React.MutableRefObject<string>, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
    this._liveContent = liveContent;
  }

  get id() { return this._current.flowId; }
  get bodyType() { return this._current.bodyType; }

  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this.id,
      changes: { ...this._current, flowValue: this._liveContent.current },
    };
  }

  withFlowValue(flowValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowValue }, this._liveContent, this._origin);
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

  const [fields, setFields] = React.useState<TextFields>({ content: '' });
  const originalContentRef = React.useRef<string>('');
  const liveContentRef = React.useRef<string>('');
  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  // Factory captures liveContentRef (the object, not .current) — always points to latest value.
  // After body loads, cancel() clears the FsuWorld entry so withNewChange re-runs the factory
  // with the loaded yaml as _origin (React 18 batches setFields + cancel before re-render).
  const state = withNewChange(props.direntId, () => new _ChangeState(
    { flowId: props.direntId, bodyType: dirent!.type, flowValue: liveContentRef.current },
    liveContentRef,
  ));

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'FLOW').then((body) => {
      const wb = body as Fs.WrenchBody;
      const yaml = wb.flows[props.direntId]?.ast?.parseTree?.value ?? '';
      originalContentRef.current = yaml;
      liveContentRef.current = yaml;
      setFields({ content: yaml });
      cancel(props.direntId);
    });
  }, [props.direntId]);

  function onChangeContent(value: string) {
    liveContentRef.current = value;
    setFields({ content: value });
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      setState(prev => prev.withFlowValue(value));
    }, 500);
  }

  function onCancel() {
    const original = originalContentRef.current;
    liveContentRef.current = original;
    setFields({ content: original });
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isChanged: state.isChanged,
    content: fields.content,
    onChangeContent,
    onCancel,
  };
};
