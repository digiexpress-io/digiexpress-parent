import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange
} from '@dxs-ts/fs-api';

export interface TextFields {
  description: string;
  urlValue: string;
  intlValues: Record<string, string>;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isLoading: boolean;
  dirent: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  id: string;
  isChanged: boolean;
  isExpanded: boolean;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  tagLabels: string[];
  configOptions: Fs.ConfigOption[];
  description: string;
  onChangeContentType: (value: string) => void;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
  onBlurDescription: () => void;
  onBlurIntlValue: (locale: string) => void;
  onBlurUrlValue: () => void;
  onCancel: () => void;
}

type _ChangeStateProps = {
  linkId: string;
  bodyType: Fs.BodyType;
  type: Fs.LinkType;
  value: string;
  labels: { locale: string; labelValue: string }[];
  tagLabels: string[];
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
  articles: string[];
  description: string;
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
  get tagLabels() { return this._current.tagLabels; }
  get configOptions() { return this._current.configOptions; }
  get articles() { return this._current.articles; }
  get description() { return this._current.description; }

  getCurrentProps(): { bodyType: Fs.BodyType, id: string, changes: Record<string, any> } {
    return { bodyType: this._current.bodyType, id: this.id, changes: this._current };
  }

  get bodyType() {
    return this._origin.bodyType;
  }
  get isChanged(): boolean {
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
  withTagLabels(tagLabels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, tagLabels }, this._origin);
  }
  withArticles(articles: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, articles }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions, devMode: configOptions.includes('DEV_MODE'), disabledMode: configOptions.includes('DISABLED_MODE') }, this._origin);
  }
  withDescription(description: string): _ChangeState {
    return new _ChangeState({ ...this._current, description }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();
  const dirent = getDirent(props.direntId);
  const linkProps = dirent?.type === 'ARTICLE_LINK' ? dirent.props as Fs.LinkProps : undefined;

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [fields, setFields] = React.useState<TextFields>({
    description: dirent?.props?.description ?? '',
    urlValue: linkProps?.urlValue ?? '',
    intlValues: linkProps?.intlValues ?? {},
  });
  const locales = selectOptions.languages;


  const state = withNewChange(props.direntId, () => new _ChangeState({
    linkId: props.direntId,
    bodyType: dirent!.type,
    type: linkProps?.contentType ?? 'internal',
    value: linkProps?.urlValue ?? '',
    labels: Object.entries(linkProps?.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue })),
    tagLabels: (linkProps?.labels ?? []).map(l => l.value),
    configOptions: (linkProps?.configOptions ?? []) as Fs.ConfigOption[],
    devMode: (linkProps?.configOptions ?? []).includes('DEV_MODE'),
    disabledMode: (linkProps?.configOptions ?? []).includes('DISABLED_MODE'),
    articles: linkProps?.articles ?? [],
    description: dirent?.props?.description ?? ''
  }));

  const isChangesPresent = state.isChanged
    || fields.description !== state.description
    || fields.urlValue !== state.urlValue
    || Object.entries(fields.intlValues).some(([locale, val]) => val !== (state.intlValues[locale] ?? ''));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value as Fs.LinkType));
  }

  function onChangeUrlValue(value: string) {
    setFields(prev => ({ ...prev, urlValue: value }));
  }

  function onChangeIntlValue(locale: string, value: string) {
    setFields(prev => ({ ...prev, intlValues: { ...prev.intlValues, [locale]: value } }));
  }

  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, description: value }));
  }

  function onChangeArticles(value: string[]) {
    setState(prev => prev.withArticles(value));
  }

  function onChangeLabels(value: string[]) {
    setState(prev => prev.withTagLabels(value));
  }

  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  function onBlurUrlValue() {
    setState(prev => prev.withUrlValue(fields.urlValue));
  }

  function onBlurIntlValue(locale: string) {
    setState(prev => prev.withIntlValue(locale, fields.intlValues[locale] ?? ''));
  }

  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.description));
  }

  function onCancel() {
    setFields({
      description: dirent?.props?.description ?? '',
      urlValue: linkProps?.urlValue ?? '',
      intlValues: linkProps?.intlValues ?? {},
    });
    cancel(props.direntId);
  }

  return ({
    isDarkMode,
    isLoading: !dirent,
    dirent,
    locales,
    id: state.id,
    isChanged: isChangesPresent,
    contentType: state.contentType,
    urlValue: fields.urlValue,
    intlValues: fields.intlValues,
    articles: state.articles,
    tagLabels: state.tagLabels,
    configOptions: state.configOptions,
    description: fields.description,
    isExpanded,
    onChangeContentType,
    onChangeUrlValue,
    onBlurUrlValue,
    onChangeIntlValue,
    onBlurIntlValue,
    onChangeArticles,
    onChangeLabels,
    onChangeConfigOptions,
    onChangeDescription,
    onBlurDescription,
    onToggleExpanded,
    onCancel,
  });
};
