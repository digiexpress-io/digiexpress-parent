import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';

type _ChangeStateProps = {
  folderId: string;
  bodyType: Fs.BodyType;
  name: string;
  treeId: string;
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
  get treeId() { return this._current.treeId; }
  get bodyType() {
    return this._current.bodyType;
  }
  get name() {
    return this._current.name;
  }
  get isDirty(): boolean {
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
  assetPath: string | undefined;
  isDirty: boolean;
  id: string;
  dirent: Fs.DirentBase | undefined;
  location: string;
  name: string;
  onChangeName: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent } = useFsDirent();


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const dirent = getDirent(props.direntId);
  const folder = dirent?.type === 'FOLDER' ? dirent : undefined;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    folderId: props.direntId,
    bodyType: dirent!.type,
    treeId: dirent?.commitIndex?.treeId!,
    name: dirent?.name ?? '',
  }));

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }

  return ({
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    id: state.id,
    dirent: folder,
    location: dirent?.fullPath ?? '',
    name: state.name,
    onChangeName,
  });
};
