import { useIntl } from 'react-intl';
import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';

import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentSelectSingleOption } from '../fs-utilities';
import { createWidget } from '../fs-factory';

export interface UpdateOwnerState {
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  id: string;
  articleName: string;
  locale: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isDirty: boolean;
  onChangeLocale: (value: string) => void;
  onChangeContent: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  content: string;
}

type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  locale: string;
  content: string;
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.pageId; }
  get treeId() { return this._current.treeId; }
  get locale() { return this._current.locale; }
  get content() { return this._current.content; }

  get configOptions() { return this._current.configOptions; }
  get bodyType() { return this._origin.bodyType; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }
  withLocale(locale: string): _ChangeState {
    return new _ChangeState({ ...this._current, locale }, this._origin);
  }
  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }
  withConfigOptions(value: string[]): _ChangeState {
    const widget = createWidget({ type: 'ARTICLE_PAGE' });
    return new _ChangeState({ ...this._current, configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)) }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const intl = useIntl();
  const { getDirent, getDirentName, selectOptions } = useFsDirent();

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PageProps;
  const articleName = getDirentName(pageProps.articleId) ?? '';
  const availableConfigOptions: Fs.SelectOption[] = createWidget({ type: 'ARTICLE_PAGE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));


  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    pageId: props.direntId,
    bodyType: dirent.type,
    treeId: dirent?.commitIndex?.treeId!,
    locale: pageProps.localeCode,
    content: pageProps.content ?? '',
    configOptions: (pageProps.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (pageProps.configOptions ?? []).includes('DEV_MODE'),
    disabledMode: (pageProps.configOptions ?? []).includes('DISABLED_MODE'),
  }));

  const usedLocaleIds = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE' && p.id !== props.direntId && (p as Fs.PageProps).articleId === pageProps.articleId)
    .map(p => (p as Fs.PageProps).localeCode);
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value) || l.value === state.locale
  );


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value))
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }

  const changes = state.isDirty;

  return ({
    assetPath: activeTabPath,
    dirent,
    id: state.id,
    content: state.content,
    locale: state.locale,
    articleName,
    configOptions: state.configOptions,
    availableConfigOptions,
    localeOptions,
    isDirty: changes,
    onChangeLocale,
    onChangeContent,
    onChangeConfigOptions,
  });
};
