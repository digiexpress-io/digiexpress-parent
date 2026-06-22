import { useFsTheme } from '../fs-theme';
import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { createWidget } from '../fs-factory';

export interface UpdateOwnerState {
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isDirty: boolean;
  name: string;
  orderNumber: string;
  configOptions: Fs.ConfigOption[];
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

type _ChangeStateProps = {
  articleId: string;
  bodyType: Fs.BodyType;
  name: string;
  order: number;
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  authOnly: boolean;
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;


  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.articleId; }
  get name() { return this._current.name; }
  get treeId() { return this._current.treeId; }
  get orderNumber() { return String(this._current.order); }
  get configOptions() { return this._current.configOptions; }
  get bodyType() { return this._current.bodyType; }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }
  withName(name: string): _ChangeState {
    return new _ChangeState({ ...this._current, name }, this._origin);
  }
  withOrder(order: string): _ChangeState {
    return new _ChangeState({ ...this._current, order: parseInt(order) || 0 }, this._origin);
  }
  withConfigOptions(value: string[]): _ChangeState {
    const widget = createWidget({ type: 'ARTICLE' });
    return new _ChangeState({
      ...this._current,
      configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)),
    }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent, getDirentName } = useFsDirent();


  const dirent = getDirent(props.direntId);
  const articleProps = dirent?.type === 'ARTICLE' ? dirent.props as Fs.ArticleProps : undefined;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    articleId: props.direntId,
    bodyType: dirent!.type,
    treeId: dirent?.commitIndex?.treeId!,
    name: getDirentName(props.direntId) ?? '',
    order: articleProps?.orderNumber ?? 0,
    configOptions: (articleProps?.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (articleProps?.configOptions ?? []).includes('DEV_MODE'),
    authOnly: (articleProps?.configOptions ?? []).includes('AUTH_ONLY_MODE'),
  }));


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }
  function onChangeOrderNumber(value: string) {
    setState(prev => prev.withOrder(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }


  return ({
    assetPath: activeTabPath,
    dirent,
    id: state.id,
    isDirty: state.isDirty,
    name: state.name,
    orderNumber: state.orderNumber,
    configOptions: state.configOptions,
    onChangeName,
    onChangeOrderNumber,
    onChangeConfigOptions,
  });
};
