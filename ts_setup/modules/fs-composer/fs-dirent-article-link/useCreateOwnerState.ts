import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  FsuCreateChange,
} from '@dxs-ts/fs-api';

export interface CreateOwnerState {
  isDarkMode: boolean;
  locales: Fs.SelectOption[];
  isChanged: boolean;
  isExpanded: boolean;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  tagLabels: string[];
  configOptions: Fs.ConfigOption[];
  assetDescription: string;
  onChangeContentType: (value: string) => void;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onToggleExpanded: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  type: Fs.LinkType;
  value: string;
  labels: { locale: string; labelValue: string }[];
  tagLabels: string[];
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
  articles: string[];
  assetDescription: { text: string };
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get contentType() { return this._current.type; }
  get urlValue() { return this._current.value; }
  get intlValues() { return Object.fromEntries(this._current.labels.map(l => [l.locale, l.labelValue])); }
  get tagLabels() { return this._current.tagLabels; }
  get configOptions() { return this._current.configOptions; }
  get articles() { return this._current.articles; }
  get assetDescription() { return this._current.assetDescription; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        value: this._current.value,
        type: this._current.type,
        labels: this._current.labels,
        articles: this._current.articles,
        devMode: this._current.devMode,
        disabledMode: this._current.disabledMode,
        assetDescription: this._current.assetDescription,
        tagLabels: this._current.tagLabels,
      }
    };
  }

  withContentType(contentType: Fs.LinkType): _CreateState {
    return new _CreateState({ ...this._current, type: contentType }, this._origin);
  }
  withUrlValue(urlValue: string): _CreateState {
    return new _CreateState({ ...this._current, value: urlValue }, this._origin);
  }
  withIntlValue(locale: string, value: string): _CreateState {
    const labels = this._current.labels.filter(l => l.locale !== locale);
    labels.push({ locale, labelValue: value });
    return new _CreateState({ ...this._current, labels }, this._origin);
  }
  withTagLabels(tagLabels: string[]): _CreateState {
    return new _CreateState({ ...this._current, tagLabels }, this._origin);
  }
  withArticles(articles: string[]): _CreateState {
    return new _CreateState({ ...this._current, articles }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _CreateState {
    return new _CreateState({ ...this._current, configOptions, devMode: configOptions.includes('DEV_MODE'), disabledMode: configOptions.includes('DISABLED_MODE') }, this._origin);
  }
  withDescription(assetDescription: { text: string }): _CreateState {
    return new _CreateState({ ...this._current, assetDescription }, this._origin);
  }
}

const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_LINK',
  type: 'internal',
  value: '',
  labels: [],
  tagLabels: [],
  configOptions: [],
  devMode: false,
  disabledMode: false,
  articles: [],
  assetDescription: { text: '' },
};


export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions } = useFsDirent();

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));
  const locales = selectOptions.languages;

  const isChangesPresent = state.isChanged;

  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value as Fs.LinkType));
  }

  function onChangeUrlValue(value: string) {
    setState(prev => prev.withUrlValue(value));
  }

  function onChangeIntlValue(locale: string, value: string) {
    setState(prev => prev.withIntlValue(locale, value));
  }

  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription({ text: value }));
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


  return ({
    isDarkMode,
    locales,
    isChanged: isChangesPresent,
    contentType: state.contentType,
    urlValue: state.urlValue,
    intlValues: state.intlValues,
    articles: state.articles,
    tagLabels: state.tagLabels,
    configOptions: state.configOptions,
    assetDescription: state.assetDescription.text,
    isExpanded,
    onChangeContentType,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeArticles,
    onChangeLabels,
    onChangeConfigOptions,
    onChangeDescription,
    onToggleExpanded,
  });
};
