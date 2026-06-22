import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';

import { useFsNav } from '@dxs-ts/fs-nav';

type _ChangeStateProps = {
  templateId: string;
  bodyType: Fs.BodyType;
  name: string;
  content: string;
  treeId: string;
}

export interface UpdateOwnerState {
  assetPath: string | undefined;
  isDirty: boolean;
  id: string;
  dirent: Fs.DirentBase | undefined;
  name: string;
  content: string;
  onChangeName: (value: string) => void;
  onChangeContent: (value: string) => void;
}


class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.templateId;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get treeId() { return this._current.treeId; }
  get name() {
    return this._current.name;
  }
  get content() {
    return this._current.content;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this._current.templateId,
      changes: {
        name: this._current.name,
        type: this._current.bodyType,
        content: this._current.content || undefined,
      },
    };
  }

  withName(name: string): _ChangeState {
    return new _ChangeState({ ...this._current, name }, this._origin);
  }
  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent } = useFsDirent();


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const dirent = getDirent(props.direntId)!;
  const templateProps = dirent.props as Fs.TemplateProps;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    templateId: props.direntId,
    bodyType: dirent!.type,
    treeId: dirent?.commitIndex?.treeId!,
    name: dirent!.name,
    content: templateProps?.content,
  }));

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }

  return ({
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    id: state.id,
    dirent,
    name: state.name,
    content: state.content,
    onChangeName,
    onChangeContent,
  });
};
