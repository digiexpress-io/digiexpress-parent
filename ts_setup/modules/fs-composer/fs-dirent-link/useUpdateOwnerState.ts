import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  id: string;
  isChanged: boolean;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  description: string;
  isExpanded: boolean;
  onChangeContentType: (value: string) => void;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
}

type _ChangeStateProps = {
  id: string;
  bodyType: Fs.BodyType;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  articles: string[];
  description: string;
  isExpanded: boolean;

}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.id; }
  get contentType() { return this._current.contentType; }
  get urlValue() { return this._current.urlValue; }
  get intlValues() { return this._current.intlValues; }
  get configOptions() { return this._current.configOptions; }
  get articles() { return this._current.articles; }
  get description() { return this._current.description; }
  get isExpanded() { return this._current.isExpanded; }

  getCurrentProps(): { bodyType: Fs.BodyType; } {
    return this._current;
  }
  get bodyType() {
    return this._origin.bodyType;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  withContentType(contentType: Fs.LinkType): _ChangeState {
    return new _ChangeState({ ...this._current, contentType }, this._origin);
  }
  withUrlValue(urlValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, urlValue }, this._origin);
  }
  withIntlValue(locale: string, value: string): _ChangeState {
    return new _ChangeState({ ...this._current, intlValues: { ...this._current.intlValues, [locale]: value } }, this._origin);
  }
  withArticles(articles: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, articles }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions }, this._origin);
  }
  withDescription(description: string): _ChangeState {
    return new _ChangeState({ ...this._current, description }, this._origin);
  }
  withIsExpanded(isExpanded: boolean): _ChangeState {
    return new _ChangeState({ ...this._current, isExpanded }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();
  const { withNewChange, withChange } = useFsu();

  const dirent = getDirent(props.direntId);
  const linkProps = dirent?.type === 'ARTICLE_LINK' ? dirent.props as Fs.LinkProps : undefined;
  const locales = selectOptions.languages;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    id: props.direntId,
    bodyType: dirent!.type,
    contentType: linkProps?.contentType ?? 'internal',
    urlValue: linkProps?.urlValue ?? '',
    intlValues: linkProps?.intlValues ?? {},
    configOptions: (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[],
    articles: linkProps?.articles ?? [],
    description: dirent?.props?.description ?? '',
    isExpanded: false,
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value as Fs.LinkType));
  }

  function onChangeUrlValue(value: string) {
    setState(prev => prev.withUrlValue(value));
  }

  function onChangeIntlValue(locale: string, value: string) {
    setState(prev => prev.withIntlValue(locale, value));
  }

  function onChangeArticles(value: string[]) {
    setState(prev => prev.withArticles(value));
  }

  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }

  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription(value));
  }

  function onToggleExpanded() {
    setState(prev => prev.withIsExpanded(!prev.isExpanded));
  }

  return ({
    isDarkMode,
    dirent,
    locales,
    id: state.id,
    isChanged: state.isChanged,
    contentType: state.contentType,
    urlValue: state.urlValue,
    intlValues: state.intlValues,
    articles: state.articles,
    configOptions: state.configOptions,
    description: state.description,
    isExpanded: state.isExpanded,
    onChangeContentType,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeArticles,
    onChangeConfigOptions,
    onChangeDescription,
    onToggleExpanded,
  });
};
