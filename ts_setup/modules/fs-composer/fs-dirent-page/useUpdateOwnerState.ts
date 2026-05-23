import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  locale: string;
  content: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
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
  get description() { return this._current.description; }
  get isExpanded() { return this._current.isExpanded; }

  get configOptions() { return this._current.configOptions; }
  get bodyType() { return this._origin.bodyType; }
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

  withDescription(description: string): _ChangeState {
    return new _ChangeState({ ...this._current, description }, this._origin);
  }

  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions, devMode: configOptions.includes('DEV_MODE'), disabledMode: configOptions.includes('DISABLED_MODE') }, this._origin);
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
  onBlurDescription: () => void;
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
    description: pageProps.description ?? '',
    configOptions: (pageProps.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (pageProps.configOptions ?? []).includes('DEV_MODE'),
    disabledMode: (pageProps.configOptions ?? []).includes('DISABLED_MODE'),
    isExpanded: false,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  // local state to store all text inputs before saving
  const [descriptionDisplay, setDescriptionDisplay] = React.useState(pageProps.description ?? '');

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
    setDescriptionDisplay(value);
  }

  function onBlurDescription() {
    setState(prev => prev.withDescription(descriptionDisplay));
  }

  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
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
    description: descriptionDisplay,
    articleName,
    configOptions: state.configOptions,
    availableConfigOptions,
    localeOptions,
    isChanged: state.isChanged || descriptionDisplay !== state.description,
    isExpanded: state.isExpanded,
    onChangeLocale,
    onChangeDescription,
    onBlurDescription,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
