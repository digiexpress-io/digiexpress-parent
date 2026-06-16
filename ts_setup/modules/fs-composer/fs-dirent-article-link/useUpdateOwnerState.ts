import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsuChange,
  FsuChange
} from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  id: string;
  isDirty: boolean;
  isExpanded: boolean;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  assetDescription: string | undefined;
  onChangeContentType: (value: string) => void;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
}

type _ChangeStateProps = {
  linkId: string;
  bodyType: Fs.BodyType;
  type: Fs.LinkType;
  value: string;
  labels: { locale: string; labelValue: string }[];
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
  articles: string[];
  assetDescription: string | undefined;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.linkId; }
  get contentType() { return this._current.type; }
  get urlValue() { return this._current.value; }
  get intlValues() { return Object.fromEntries(this._current.labels.map(l => [l.locale, l.labelValue])); }
  get configOptions() { return this._current.configOptions; }
  get articles() { return this._current.articles; }
  get assetDescription() { return this._current.assetDescription; }

  getCurrentProps(): { bodyType: Fs.BodyType, id: string, changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }
  get bodyType() {
    return this._origin.bodyType;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }
  withContentType(contentType: Fs.LinkType): _ChangeState {
    return new _ChangeState({ ...this._current, type: contentType }, this._origin);
  }
  withUrlValue(urlValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, value: urlValue }, this._origin);
  }
  withIntlValue(locale: string, value: string): _ChangeState {
    const labels = this._current.labels.filter(l => l.locale !== locale);
    labels.push({ locale, labelValue: value });
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
  withArticles(articles: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, articles }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions, devMode: configOptions.includes('DEV_MODE'), disabledMode: configOptions.includes('DISABLED_MODE') }, this._origin);
  }
  withDescription(assetDescription: string | undefined): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();
  const dirent = getDirent(props.direntId);
  const linkProps = dirent?.type === 'ARTICLE_LINK' ? dirent.props as Fs.LinkProps : undefined;

  const [isExpanded, setIsExpanded] = React.useState(false);
  const locales = selectOptions.languages;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    linkId: props.direntId,
    bodyType: dirent!.type,
    type: linkProps?.contentType ?? 'internal',
    value: linkProps?.urlValue ?? '',
    labels: Object.entries(linkProps?.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue })),
    configOptions: (linkProps?.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (linkProps?.configOptions ?? []).includes('DEV_MODE'),
    disabledMode: (linkProps?.configOptions ?? []).includes('DISABLED_MODE'),
    articles: linkProps?.articles ?? [],
    assetDescription: linkProps?.assetDescription,
  }));

  const isChangesPresent = state.isDirty;

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value as Fs.LinkType));
  }
  function onChangeUrlValue(value: string) {
    setState(prev => prev.withUrlValue(value)) 
  }
  function onChangeIntlValue(locale: string, value: string) {
    setState(prev => prev.withIntlValue(locale, value)) 
  }
  function onChangeArticles(value: string[]) {
    setState(prev => prev.withArticles(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }


  return ({
    isDarkMode,
    assetPath: activeTabPath,
    dirent,
    locales,
    id: state.id,
    isDirty: isChangesPresent,
    contentType: state.contentType,
    urlValue: state.urlValue,
    intlValues: state.intlValues,
    articles: state.articles,
    configOptions: state.configOptions,
    assetDescription: state.assetDescription,
    isExpanded,
    onChangeContentType,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeArticles,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
