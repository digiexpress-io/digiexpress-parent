import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  assetPath: string | undefined;
  isDirty: boolean;
  content: string;
  connectedResourceNames: string[];
  onChangeContent: (value: string) => void;
}

type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  content: string;
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
    return this._current.pageId;
  }
  get treeId() { return this._current.treeId; }
  get bodyType() {
    return this._current.bodyType;
  }
  get content() {
    return this._current.content;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.pageId,
      changes: {
        pageId: c.pageId,
        content: c.content || undefined,
      },
    };
  }

  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();


  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PrintoutPageProps;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    pageId: props.direntId,
    bodyType: dirent.type,
    treeId: dirent?.commitIndex?.treeId!,
    content: pageProps.content ?? '',
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);


  const connectedResourceNames = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_RESOURCE')
    .map(p => p as Fs.PrintoutResourceProps)
    .filter(p => p.printoutPageIds.includes(props.direntId))
    .map(p => p.resourceName);

  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }

  return {
    isDarkMode,
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    content: state.content,
    connectedResourceNames,
    onChangeContent,
  };
};
