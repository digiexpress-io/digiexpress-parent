import { Fs, FsuChange, useFsuChange, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useFsTheme } from '../fs-theme';

type _ChangeStateProps = {
  direntId: string;
  bodyType: Fs.BodyType;
  labels: string[];
}

export interface OwnerState {
  isDarkMode: boolean;
  labels: string[];
  labelOptions: string[];
  isDirty: boolean;
  onChangeLabels: (values: string[]) => void;
  onSave: () => Promise<void>;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.direntId;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get labels() {
    return this._current.labels;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.direntId,
      changes: {
        labels: c.labels,
      },
    };
  }

  withLabels(labels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
}

export const useOwnerState = (props: FsDirentLabelsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { dirent } = props;
  const { updateDirentLabels, selectOptions } = useFsDirent();

  const { state, update } = useFsuChange(dirent.id, () => new _ChangeState({
    direntId: dirent.id,
    bodyType: dirent.type,
    labels: (dirent.props?.labels ?? []).map(l => l.key),
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  function onChangeLabels(values: string[]) {
    setState(prev => prev.withLabels(values));
  }

  async function onSave() {
    await updateDirentLabels(dirent.id, state.labels.map(key => ({ key })));
  }

  return {
    isDarkMode,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    isDirty: state.isDirty,
    onChangeLabels,
    onSave,
  };
};
