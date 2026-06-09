import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  isLoading: boolean;
  id: string;
  articleName: string;
  locale: string;
  assetDescription: string;
  configOptions: Fs.ConfigOption[];
  labels: string[];
  labelOptions: string[];
  availableConfigOptions: Fs.SelectOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isChanged: boolean;
  isExpanded: boolean;
  onChangeLocale: (value: string) => void;
  onChangeContent: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
  content: string;
}

type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  locale: string;
  content: string;
  assetDescription: { text: string };
  configOptions: Fs.ConfigOption[];
  labels: string[];
  devMode: boolean;
  disabledMode: boolean;
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
  get assetDescription() { return this._current.assetDescription; }

  get configOptions() { return this._current.configOptions; }
  get labels() { return this._current.labels; }
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
  withDescription(assetDescription: { text: string }): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({
      ...this._current, configOptions,
      devMode: configOptions.includes('DEV_MODE'),
      disabledMode: configOptions.includes('DISABLED_MODE')
    }, this._origin);
  }
  withLabels(labels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, getDirentName, selectOptions, getConfigOptionsForType } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const [isExpanded, setIsExpanded] = React.useState(false);

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PageProps;
  const articleName = getDirentName(pageProps.articleId) ?? '';
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');

  const state = withNewChange(props.direntId, () => new _ChangeState({
    pageId: props.direntId,
    bodyType: dirent.type,
    locale: pageProps.localeCode,
    content: pageProps.content ?? '',
    assetDescription: { text: pageProps.assetDescription ?? '' },
    configOptions: (pageProps.configOptions ?? []) as Fs.ConfigOption[],
    labels: (pageProps.labels ?? []).map(l => l.value),
    devMode: (pageProps.configOptions ?? []).includes('DEV_MODE'),
    disabledMode: (pageProps.configOptions ?? []).includes('DISABLED_MODE'),
  }));

  const usedLocaleIds = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE' && p.id !== props.direntId && (p as Fs.PageProps).articleId === pageProps.articleId)
    .map(p => (p as Fs.PageProps).localeCode);
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value) || l.value === state.locale
  );


  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value))
  }
  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription({ text: value }));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }
  function onCancel() {
    cancel(props.direntId);
  }

  const changes = state.isChanged;

  return ({
    isDarkMode,
    dirent,
    isLoading: !dirent,
    id: state.id,
    content: state.content,
    locale: state.locale,
    assetDescription: state.assetDescription.text,
    articleName,
    configOptions: state.configOptions,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    availableConfigOptions,
    localeOptions,
    isChanged: changes,
    isExpanded,
    onChangeLocale,
    onChangeContent,
    onChangeDescription,
    onChangeConfigOptions,
    onChangeLabels,
    onToggleExpanded,
    onCancel,
  });
};
