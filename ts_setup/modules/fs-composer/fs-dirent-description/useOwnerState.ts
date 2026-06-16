import { Fs, FsuChange, useFsuChange, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentDescriptionProps } from './FsDirentDescriptionProps';
import { useFsTheme } from '../fs-theme';

type _ChangeStateProps = {
  direntId: string;
  bodyType: Fs.BodyType;
  assetDescription: string | undefined;
}

export interface OwnerState {
  isDarkMode: boolean;
  description: string;
  isDirty: boolean;
  onChangeDescription: (value: string) => void;
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
  get assetDescription() {
    return this._current.assetDescription;
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
        assetDescription: c.assetDescription || undefined,
      },
    };
  }

  withDescription(assetDescription: string | undefined): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }
}

export const useOwnerState = (props: FsDirentDescriptionProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { dirent } = props;
  const { updateDirentDescription } = useFsDirent();

  const { state, update } = useFsuChange(dirent.id, () => new _ChangeState({
    direntId: dirent.id,
    bodyType: dirent.type,
    assetDescription: dirent.props?.assetDescription,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription(value));
  }

  async function onSave() {
    await updateDirentDescription(dirent.id, state.assetDescription || undefined);
  }

  return {
    isDarkMode,
    description: state.assetDescription ?? '',
    isDirty: state.isDirty,
    onChangeDescription,
    onSave,
  };
};
