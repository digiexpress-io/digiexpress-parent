import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  locale: string;
  content: string;
  isExpanded: boolean;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.pageId; }
  get locale() { return this._current.locale; }
  get content() { return this._current.content; }
  get isExpanded() { return this._current.isExpanded; }

  get bodyType() { return this._current.bodyType; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }

  withLocale(locale: string): _ChangeState {
    return new _ChangeState({ ...this._current, locale }, this._origin);
  }

  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }

  withIsExpanded(isExpanded: boolean): _ChangeState {
    return new _ChangeState({ ...this._current, isExpanded }, this._origin);
  }
}


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  articleName: string;
  locale: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isChanged: boolean;
  isExpanded: boolean;
  onChangeLocale: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
  content: string;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, getArticleName, selectOptions, getConfigOptionsForType, fetchDirentBody } = useFsDirent();
  const { withNewChange, withChange } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PageProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    pageId: props.direntId,
    bodyType: dirent.type,
    locale: pageProps.localeCode,
    content: pageProps.content ?? '',
    isExpanded: false,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [description, setDescription] = React.useState(pageProps.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );

  const articleName = getArticleName(pageProps.articleId) ?? '';
  const usedLocaleIds = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE' && p.id !== props.direntId && (p as Fs.PageProps).articleId === pageProps.articleId)
    .map(p => (p as Fs.PageProps).localeCode);
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value) || l.value === state.locale
  );
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'ARTICLE_PAGE')
      .then(body => {
        const c = (body as Fs.ArticlePageBody).content;
        setState(prev => prev.withContent(c));
      });
  }, [props.direntId]);

  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  function onToggleExpanded() {
    setState(prev => prev.withIsExpanded(!prev.isExpanded));
  }

  return ({
    isDarkMode,
    dirent,
    id: state.id,
    content: state.content,
    locale: state.locale,
    description,
    articleName,
    configOptions,
    availableConfigOptions,
    localeOptions,
    isChanged: state.isChanged,
    isExpanded: state.isExpanded,
    onChangeLocale,
    onChangeDescription,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
