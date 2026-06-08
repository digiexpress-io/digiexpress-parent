import React from 'react';
import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


export interface CreateOwnerState {
  isDarkMode: boolean;
  articleId: string;
  locale: string;
  content: string;
  assetDescription: string;
  configOptions: Fs.ConfigOption[];
  labels: string[];
  labelOptions: string[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isChanged: boolean;
  isExpanded: boolean;
  onChangeArticle: (value: string) => void;
  onChangeLocale: (value: string) => void;
  onChangeContent: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onToggleExpanded: () => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  articleId: string;
  locale: string;
  content: string;
  assetDescription: { text: string };
  configOptions: Fs.ConfigOption[];
  labels: string[];
  devMode: boolean;
  disabledMode: boolean;
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get articleId() { return this._current.articleId; }
  get locale() { return this._current.locale; }
  get content() { return this._current.content; }
  get assetDescription() { return this._current.assetDescription; }
  get configOptions() { return this._current.configOptions; }
  get labels() { return this._current.labels; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        articleId: this._current.articleId,
        locale: this._current.locale,
        content: this._current.content,
        assetDescription: this._current.assetDescription,
        labels: this._current.labels.length ? this._current.labels : undefined,
        devMode: this._current.devMode,
        disabledMode: this._current.disabledMode,
      }
    };
  }

  withArticle(articleId: string): _CreateState {
    return new _CreateState({ ...this._current, articleId }, this._origin);
  }
  withLocale(locale: string): _CreateState {
    return new _CreateState({ ...this._current, locale }, this._origin);
  }
  withContent(content: string): _CreateState {
    return new _CreateState({ ...this._current, content }, this._origin);
  }
  withDescription(assetDescription: { text: string }): _CreateState {
    return new _CreateState({ ...this._current, assetDescription }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _CreateState {
    return new _CreateState({
      ...this._current,
      configOptions,
      devMode: configOptions.includes('DEV_MODE'),
      disabledMode: configOptions.includes('DISABLED_MODE'),
    }, this._origin);
  }
  withLabels(labels: string[]): _CreateState {
    return new _CreateState({ ...this._current, labels }, this._origin);
  }
}


const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_PAGE',
  articleId: '',
  locale: '',
  content: '',
  assetDescription: { text: '' },
  configOptions: [],
  labels: [],
  devMode: false,
  disabledMode: false,
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions, getDirentName, getConfigOptionsForType } = useFsDirent();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  const usedLocaleIds = state.articleId ? Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE' && (p as Fs.PageProps).articleId === state.articleId)
    .map(p => (p as Fs.PageProps).localeCode)
    : [];

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles.map(item => ({
    value: item.value,
    label: getDirentName(item.value) ?? item.label,
  }));

  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value)
  );

  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');
  const isChangesPresent = state.isChanged;

  function onChangeArticle(value: string) {
    setState(prev => prev.withArticle(value));
  }
  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
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

  async function onSave() {
    try {
      const dirent = await pushCreate(state);
      openAsset(dirent);
    } catch {
      // error snackbar already shown by handlePushCreate
    }
  }

  function onCancel() {
    setState(new _CreateState(_init))
  }

  return ({
    isDarkMode,
    articleId: state.articleId,
    locale: state.locale,
    content: state.content,
    assetDescription: state.assetDescription.text,
    configOptions: state.configOptions,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    isChanged: isChangesPresent,
    isExpanded,
    onChangeArticle,
    onChangeLocale,
    onChangeContent,
    onChangeDescription,
    onChangeConfigOptions,
    onChangeLabels,
    onToggleExpanded,
    onSave,
    onCancel,
  });
};
