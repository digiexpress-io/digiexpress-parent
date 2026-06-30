import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';

import { useFsNav } from '@dxs-ts/fs-nav';


type _ChangeStateProps = {
  localeId: string;
  bodyType: Fs.BodyType;
  value: string;
  enabled: boolean;
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.localeId; }
  get treeId() { return this._current.treeId; }
  get bodyType() { return this._current.bodyType; }
  get value() { return this._current.value; }
  get enabled() { return this._current.enabled; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: this.id,
      changes: {
        localeId: c.localeId,
        value: c.value,
        enabled: c.enabled,
      }
    };
  }
  withEnabled(enabled: boolean): _ChangeState {
    return new _ChangeState({ ...this._current, enabled }, this._origin);
  }
  withLocaleCode(value: string): _ChangeState {
    return new _ChangeState({ ...this._current, value }, this._origin);
  }

}


export interface UpdateOwnerState {
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  id: string;
  localeCode: string;
  enabled: boolean;
  isDirty: boolean;
  onChangeEnabled: (value: boolean) => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent } = useFsDirent();


  const dirent = getDirent(props.direntId)!;
  const languageProps = dirent.props as Fs.LanguageProps;

  const { state, update, cancel } = useFsuChange(props.direntId, () => new _ChangeState({
    localeId: props.direntId,
    bodyType: dirent.type,
    treeId: dirent?.commitIndex?.treeId!,
    value: languageProps.localeCode,
    enabled: languageProps.enabled ?? true,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback)


  function onChangeEnabled(value: boolean) {
    setState(prev => prev.withEnabled(value));
  }
  function onCancel() {
    cancel();
  }

  const changes = state.isDirty;

  return ({
    assetPath: activeTabPath,
    dirent,
    id: state.id,
    localeCode: state.value,
    enabled: state.enabled,
    isDirty: changes,
    onChangeEnabled,
    onCancel,
  });
};
