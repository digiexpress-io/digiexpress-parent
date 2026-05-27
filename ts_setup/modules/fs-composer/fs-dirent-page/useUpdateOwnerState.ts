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

}


export interface TextFields {
  content: string;
  description: string;
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
  onChangeContent: (value: string) => void;
  onBlurContent: () => void;
  onChangeDescription: (value: string) => void;
  onBlurDescription: () => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
  content: string;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, getArticleName, selectOptions, getConfigOptionsForType } = useFsDirent();
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
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [fields, setFields] = React.useState<TextFields>({
    content: pageProps.content ?? '',
    description: pageProps.description ?? '',
  });
  const contentDebounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const [isExpanded, setIsExpanded] = React.useState(false);

  const articleName = getArticleName(pageProps.articleId) ?? '';
  const usedLocaleIds = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE' && p.id !== props.direntId && (p as Fs.PageProps).articleId === pageProps.articleId)
    .map(p => (p as Fs.PageProps).localeCode);
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value) || l.value === state.locale
  );
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');

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

  const changes = state.isChanged
    || fields.description !== state.description
    || fields.content !== state.content;

  return ({
    isDarkMode,
    dirent,
    id: state.id,
    content: fields.content,
    locale: state.locale,
    description: fields.description,
    articleName,
    configOptions: state.configOptions,
    availableConfigOptions,
    localeOptions,
    isChanged: changes,
    isExpanded,
    onChangeLocale,
    onChangeContent,
    onChangeDescription,
    onChangeConfigOptions,
    onToggleExpanded,
    onBlurContent,
    onBlurDescription,
  });
};
