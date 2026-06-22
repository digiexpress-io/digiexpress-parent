import React from 'react';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectSingleOption } from '../fs-utilities';
import { createWidget } from '../fs-factory';


export interface CreateOwnerState {
  articleId: string;
  templateId: string;
  locale: string;
  content: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  templateOptions: FsDirentSelectSingleOption[];
  isDirty: boolean;
  onChangeArticle: (value: string) => void;
  onChangeLocale: (value: string) => void;
  onChangeContent: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeTemplate: (value: string) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  articleId: string;
  templateId: string;
  locale: string;
  content: string;
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
  get templateId() { return this._current.templateId; }
  get locale() { return this._current.locale; }
  get content() { return this._current.content; }
  get configOptions() { return this._current.configOptions; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        articleId: this._current.articleId,
        locale: this._current.locale,
        content: this._current.content,
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
  withConfigOptions(value: string[]): _CreateState {
    const widget = createWidget({ type: 'ARTICLE_PAGE' });
    return new _CreateState({ ...this._current, configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)) }, this._origin);
  }
  withTemplate(templateId: string, content: string): _CreateState {
    return new _CreateState({ ...this._current, templateId, content }, this._origin);
  }
}


const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_PAGE',
  articleId: '',
  templateId: '',
  locale: '',
  content: '',
  configOptions: [],
  devMode: false,
  disabledMode: false,
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const intl = useIntl();
  const { selectOptions, getDirentName, createDirent } = useFsDirent();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  const usedLocaleIds = state.articleId ? Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE' && (p as Fs.PageProps).articleId === state.articleId)
    .map(p => (p as Fs.PageProps).localeCode)
    : [];

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles.map(item => ({
    value: item.value,
    label: getDirentName(item.value) ?? item.label,
  }));

  const templateOptions: FsDirentSelectSingleOption[] = Object.entries(selectOptions.direntProps)
    .filter(([_id, props]) => props.type === 'ARTICLE_TEMPLATE')
    .map(([id]) => ({ value: id, label: getDirentName(id) ?? id }));

  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value)
  );

  const availableConfigOptions: Fs.SelectOption[] = createWidget({ type: 'ARTICLE_PAGE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));
  const isChangesPresent = state.isDirty;

  function onChangeArticle(value: string) {
    setState(prev => prev.withArticle(value));
  }
  function onChangeLocale(value: string) {
    setState(prev => prev.withLocale(value));
  }
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onChangeTemplate(value: string) {
    const templateProps = selectOptions.direntProps[value] as Fs.TemplateProps | undefined;
    setState(prev => prev.withTemplate(value, templateProps?.content ?? ''));
  }

  async function onSave() {
    await createDirent(state);
  }

  return ({
    articleId: state.articleId,
    templateId: state.templateId,
    locale: state.locale,
    content: state.content,
    configOptions: state.configOptions,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    templateOptions,
    isDirty: isChangesPresent,
    onChangeArticle,
    onChangeLocale,
    onChangeContent,
    onChangeConfigOptions,
    onChangeTemplate,
    onSave,
  });
};
