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

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.flowId; }
  get bodyType() { return this._current.bodyType; }
  get flowValue() { return this._current.flowValue; }

  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }

  withFlowValue(flowValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowValue }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, fetchDirentBody } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId);
  const originalContentRef = React.useRef<string>('');

  const [fields, setFields] = React.useState<TextFields>({ content: '' });

  // Factory captures fields.content — after body load, cancel() clears the entry so
  // withNewChange re-runs the factory with the loaded yaml as _origin.
  const state = withNewChange(props.direntId, () => new _ChangeState({
    flowId: props.direntId,
    bodyType: dirent!.type,
    flowValue: fields.content,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'FLOW').then((body) => {
      const wb = body as Fs.WrenchBody;
      const yaml = wb.flows[props.direntId]?.ast?.parseTree?.value ?? '';
      originalContentRef.current = yaml;
      setFields({ content: yaml });
      cancel(props.direntId);
    });
  }, [props.direntId]);

  function onChangeContent(value: string) {
    setFields({ content: value });
    setState(prev => prev.withFlowValue(value));
  }

  function onCancel() {
    setFields({ content: originalContentRef.current });
    cancel(props.direntId);
  }

  const isChanged = state.isChanged || fields.content !== state.flowValue;

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isChanged,
    content: fields.content,
    onChangeContent,
    onCancel,
  };
};
