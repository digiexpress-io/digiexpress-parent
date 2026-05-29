import React from 'react';
import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';

export interface TextFields {
  content: string;
  description: string;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  articleId: string;
  locale: string;
  content: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isChanged: boolean;
  isExpanded: boolean;
  onChangeArticle: (value: string) => void;
  onChangeLocale: (value: string) => void;
  onChangeContent: (value: string) => void;
  onBlurContent: () => void;
  onChangeDescription: (value: string) => void;
  onBlurDescription: () => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  articleId: string;
  locale: string;
  content: string;
  description: string;
  configOptions: Fs.ConfigOption[];
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
  get description() { return this._current.description; }
  get configOptions() { return this._current.configOptions; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        articleId: this._current.articleId,
        locale: this._current.locale,
        content: this._current.content,
        description: this._current.description,
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
  withDescription(description: string): _CreateState {
    return new _CreateState({ ...this._current, description }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _CreateState {
    return new _CreateState({
      ...this._current,
      configOptions,
      devMode: configOptions.includes('DEV_MODE'),
      disabledMode: configOptions.includes('DISABLED_MODE'),
    }, this._origin);
  }
}

const _initFields: TextFields = { content: '', description: '' };

const _initProps: _CreateStateProps = {
  bodyType: 'ARTICLE_PAGE',
  articleId: '',
  locale: '',
  content: '',
  description: '',
  configOptions: [],
  devMode: false,
  disabledMode: false,
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions, getArticleName, getConfigOptionsForType } = useFsDirent();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [fields, setFields] = React.useState<TextFields>(_initFields);
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));
  const contentDebounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles.map(item => ({
    value: item.value,
    label: getArticleName(item.value) ?? item.label,
  }));

  const usedLocaleIds = state.articleId
    ? Object.values(selectOptions.direntProps)
      .filter(p => p.type === 'ARTICLE_PAGE' && (p as Fs.PageProps).articleId === state.articleId)
      .map(p => (p as Fs.PageProps).localeCode)
    : [];
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value)
  );

  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');

  const isChangesPresent = state.isChanged
    || fields.content !== state.content
    || fields.description !== state.description;

  function onChangeArticle(value: string) {
    setState(prev => prev.withArticle(value));
  }

  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }

  function onChangeContent(value: string) {
    setFields(prev => ({ ...prev, content: value }));
    if (contentDebounceRef.current) {
      clearTimeout(contentDebounceRef.current);
    }
    contentDebounceRef.current = setTimeout(() => {
      setState(prev => prev.withContent(value));
    }, 300);
  }

  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, description: value }));
  }

  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  function onBlurContent() {
    if (contentDebounceRef.current) {
      clearTimeout(contentDebounceRef.current);
    }
    setState(prev => prev.withContent(fields.content));
  }

  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.description));
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
    if (contentDebounceRef.current) {
      clearTimeout(contentDebounceRef.current);
    }
    setFields(_initFields);
    setStateRaw(new _CreateState(_initProps));
  }

  return ({
    isDarkMode,
    articleId: state.articleId,
    locale: state.locale,
    content: fields.content,
    description: fields.description,
    configOptions: state.configOptions,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    isChanged: isChangesPresent,
    isExpanded,
    onChangeArticle,
    onChangeLocale,
    onChangeContent,
    onBlurContent,
    onChangeDescription,
    onBlurDescription,
    onChangeConfigOptions,
    onToggleExpanded,
    onSave,
    onCancel,
  });
};
