import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsuChange,
  FsuChange
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isDirty: boolean;
  taskValue: string;
  onChangeTaskValue: (value: string) => void;
}

type _ChangeStateProps = {
  flowTaskId: string;
  bodyType: Fs.BodyType;
  flowTaskValue: string;
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.flowTaskId; }
  get treeId() { return this._current.treeId; }
  get bodyType() { return this._current.bodyType; }
  get flowTaskValue() { return this._current.flowTaskValue; }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this.id,
      changes: {
        flowTaskId: this.id,
        flowTaskValue: this._current.flowTaskValue,
      },
    };
  }

  withFlowTaskValue(flowTaskValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowTaskValue }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const dirent = getDirent(props.direntId);
  const flowTaskProps = dirent?.props as Fs.FlowTaskProps | undefined;


  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    flowTaskId: props.direntId,
    bodyType: flowTaskProps!.type,
    treeId: dirent?.commitIndex?.treeId!,
    flowTaskValue: flowTaskProps?.taskValue ?? '',
  }));

  function onChangeTaskValue(value: string) {
    setState(prev => prev.withFlowTaskValue(value));
  }


  return {
    isDarkMode,
    dirent,
    id: state.id,
    isDirty: state.isDirty,
    taskValue: state.flowTaskValue,
    onChangeTaskValue,
  };
};
