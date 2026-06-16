import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';

type _ChangeStateProps = {
  templateId: string;
  bodyType: Fs.BodyType;
  content: string;
  assetDescription: string | undefined;
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
  get content() {
    return this._current.content;
  }
  get assetDescription() { return this._current.assetDescription; }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this._current.templateId,
      changes: {
        content: this._current.content || undefined,
        assetDescription: this._current.assetDescription || undefined,
      },
    };
  }

  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  assetPath: string | undefined;
  isDirty: boolean;
  id: string;
  dirent: Fs.DirentBase | undefined;
  content: string;
  assetDescription: string | undefined;
  onChangeContent: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { getDirent } = useFsDirent();


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const dirent = getDirent(props.direntId);
  const templateProps = dirent?.type === 'ARTICLE_TEMPLATE' ? dirent.props as Fs.TemplateProps : undefined;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    templateId: props.direntId,
    bodyType: dirent!.type,
    content: templateProps?.content ?? '',
    assetDescription: templateProps?.assetDescription,
  }));

  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }

  return ({
    isDarkMode,
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    id: state.id,
    dirent,
    content: state.content,
    assetDescription: state.assetDescription,
    onChangeContent,
  });
};
