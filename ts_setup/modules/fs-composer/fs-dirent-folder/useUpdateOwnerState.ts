import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';

type _ChangeStateProps = {
  folderId: string;
  bodyType: Fs.BodyType;
  name: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.folderId;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get name() {
    return this._current.name;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this._current.folderId,
      changes: {
        name: this._current.name || undefined,
      },
    };
  }

  withName(name: string): _ChangeState {
    return new _ChangeState({ ...this._current, name }, this._origin);
  }
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  id: string;
  dirent: Fs.DirentBase | undefined;
  location: string;
  name: string;
  onChangeName: (value: string) => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const dirent = getDirent(props.direntId);
  const folder = dirent?.type === 'FOLDER' ? dirent : undefined;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    folderId: props.direntId,
    bodyType: dirent!.type,
    name: dirent?.name ?? '',
  }));

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }

  function onCancel() {
    cancel(props.direntId);
  }

  return ({
    isDarkMode,
    isChanged: state.isChanged,
    id: state.id,
    dirent: folder,
    location: dirent?.fullPath ?? '',
    name: state.name,
    onChangeName,
    onCancel,
  });
};
