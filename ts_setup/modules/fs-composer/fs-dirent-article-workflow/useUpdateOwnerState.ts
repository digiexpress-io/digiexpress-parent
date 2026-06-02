import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


type _ChangeStateProps = {
  workflowId: string;
  bodyType: Fs.BodyType;
  value: string;
  description: string;
  formName: string;
  formTag: string;
  flowName: string;
  validityStart: string;
  validityEnd: string;
  articles: string[];
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  tagLabels: string[];
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.workflowId; }
  get bodyType() { return this._current.bodyType; }
  get value() { return this._current.value; }
  get description() { return this._current.description; }
  get intlValues() { return this._current.intlValues; }
  get validityStart() { return this._current.validityStart; }
  get validityEnd() { return this._current.validityEnd; }
  get tagLabels() { return this._current.tagLabels; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: this.id,
      changes: {
        workflowId: c.workflowId,
        value: c.value,
        description: c.description || undefined,
        formName: c.formName || undefined,
        formTag: c.formTag || undefined,
        flowName: c.flowName || undefined,
        startDate: c.validityStart || undefined,
        endDate: c.validityEnd || undefined,
        articles: c.articles,
        labels: Object.entries(c.intlValues).map(([locale, labelValue]) => ({ locale, labelValue })),
        tagLabels: c.tagLabels,
        disabled: c.configOptions.includes('DISABLED_MODE') || undefined,
        devMode: c.configOptions.includes('DEV_MODE') || undefined,
        anon: c.configOptions.includes('ANONYMOUS_MODE') || undefined,
        assignable: c.configOptions.includes('ASSIGNABLE_MODE') || undefined,
        authOnly: c.configOptions.includes('AUTH_ONLY_MODE') || undefined,
      }
    };
  }

  withValue(value: string): _ChangeState {
    return new _ChangeState({ ...this._current, value }, this._origin);
  }

  withDescription(description: string): _ChangeState {
    return new _ChangeState({ ...this._current, description }, this._origin);
  }

  withFormName(formName: string): _ChangeState {
    return new _ChangeState({ ...this._current, formName }, this._origin);
  }

  withFormTag(formTag: string): _ChangeState {
    return new _ChangeState({ ...this._current, formTag }, this._origin);
  }

  withFlowName(flowName: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowName }, this._origin);
  }

  withValidityStart(validityStart: string): _ChangeState {
    return new _ChangeState({ ...this._current, validityStart }, this._origin);
  }

  withValidityEnd(validityEnd: string): _ChangeState {
    return new _ChangeState({ ...this._current, validityEnd }, this._origin);
  }

  withArticles(articles: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, articles }, this._origin);
  }

  withIntlValues(locale: string, labelValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, intlValues: { ...this._current.intlValues, [locale]: labelValue } }, this._origin);
  }

  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions }, this._origin);
  }

  withTagLabels(tagLabels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, tagLabels }, this._origin);
  }
}


export interface TextFields {
  name: string;
  description: string;
  formName: string;
  formTag: string;
  flowName: string;
  validityStart: string;
  validityEnd: string;
  articles: string[];
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  tagLabels: string[];
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  name: string;
  description: string;
  dialobFormName: string;
  dialobFormTag: string;
  flowName: string;
  validityStart: string;
  validityEnd: string;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  tagLabels: string[];
  intlValues: Record<string, string>;
  locales: Fs.SelectOption[];
  isExpanded: boolean;
  isChanged: boolean;
  onChangeName: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeDialobFormName: (value: string) => void;
  onChangeDialobFormTag: (value: string) => void;
  onChangeFlowName: (value: string) => void;
  onChangeValidityStart: (date: Date | undefined) => void;
  onChangeValidityEnd: (date: Date | undefined) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeIntlValues: (locale: string, value: string) => void;
  onChangeLabels: (value: string[]) => void;
  onBlurIntlValues: (locale: string) => void;
  onBlurName: () => void;
  onBlurDescription: () => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();
  const { withNewChange, withChange, cancel, isChange } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const workflowProps = dirent.props as Fs.WorkflowProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    workflowId: props.direntId,
    bodyType: dirent.type,
    value: dirent.name ?? '',
    description: workflowProps.description ?? '',
    formName: workflowProps.dialobFormName ?? '',
    formTag: workflowProps.dialobFormTag ?? '',
    flowName: workflowProps.flowName ?? '',
    validityStart: workflowProps.validityStart ?? '',
    validityEnd: workflowProps.validityEnd ?? '',
    articles: (workflowProps.articles ?? []),
    intlValues: (workflowProps.intlValues ?? {}),
    configOptions: (workflowProps.configOptions ?? []) as Fs.ConfigOption[],
    tagLabels: (workflowProps.labels ?? []).map((l: any) => l.value),
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [fields, setFields] = React.useState<TextFields>({
    name: dirent.name ?? '',
    description: workflowProps.description ?? '',
    formName: workflowProps.dialobFormName ?? '',
    formTag: workflowProps.dialobFormTag ?? '',
    flowName: workflowProps.flowName ?? '',
    validityStart: workflowProps.validityStart ?? '',
    validityEnd: workflowProps.validityEnd ?? '',
    articles: (workflowProps.articles ?? []),
    intlValues: (workflowProps.intlValues ?? {}),
    configOptions: (workflowProps.configOptions ?? []) as Fs.ConfigOption[],
    tagLabels: (workflowProps.labels ?? []).map((l: any) => l.value),
  });

  const [isExpanded, setIsExpanded] = React.useState(false);

  const locales = selectOptions.languages;

  function onChangeName(value: string) {
    setFields(prev => ({ ...prev, name: value }));
  }

  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, description: value }));
  }

  function onChangeIntlValues(locale: string, value: string) {
    setFields(prev => ({ ...prev, intlValues: { ...prev.intlValues, [locale]: value } }));
  }

  function onChangeDialobFormName(value: string) {
    setFields(prev => ({ ...prev, formName: value }));
    setState(prev => prev.withFormName(value));
  }

  function onChangeDialobFormTag(value: string) {
    setFields(prev => ({ ...prev, formTag: value }));
    setState(prev => prev.withFormTag(value));
  }

  function onChangeFlowName(value: string) {
    setFields(prev => ({ ...prev, flowName: value }));
    setState(prev => prev.withFlowName(value));
  }

  function onChangeValidityStart(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setFields(prev => ({ ...prev, validityStart: iso }));
    setState(prev => prev.withValidityStart(iso));
  }

  function onChangeValidityEnd(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setFields(prev => ({ ...prev, validityEnd: iso }));
    setState(prev => prev.withValidityEnd(iso));
  }

  function onChangeArticles(value: string[]) {
    setFields(prev => ({ ...prev, articles: value }));
    setState(prev => prev.withArticles(value));
  }

  function onChangeConfigOptions(value: string[]) {
    const opts = value as Fs.ConfigOption[];
    setFields(prev => ({ ...prev, configOptions: opts }));
    setState(prev => prev.withConfigOptions(opts));
  }

  function onChangeLabels(value: string[]) {
    setFields(prev => ({ ...prev, tagLabels: value }));
    setState(prev => prev.withTagLabels(value));
  }

  function onBlurName() {
    setState(prev => prev.withValue(fields.name));
  }

  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.description));
  }

  function onBlurIntlValues(locale: string) {
    setState(prev => prev.withIntlValues(locale, fields.intlValues[locale] ?? ''));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  function onCancel() {
    setFields({
      name: dirent.name ?? '',
      description: workflowProps.description ?? '',
      formName: workflowProps.dialobFormName ?? '',
      formTag: workflowProps.dialobFormTag ?? '',
      flowName: workflowProps.flowName ?? '',
      validityStart: workflowProps.validityStart ?? '',
      validityEnd: workflowProps.validityEnd ?? '',
      articles: (workflowProps.articles ?? []),
      intlValues: (workflowProps.intlValues ?? {}),
      configOptions: (workflowProps.configOptions ?? []) as Fs.ConfigOption[],
      tagLabels: (workflowProps.labels ?? []).map((l: any) => l.value),
    });
    cancel(props.direntId);
  }

  const changes = state.isChanged
    || fields.name !== state.value
    || fields.description !== state.description
    || JSON.stringify(fields.intlValues) !== JSON.stringify(state.intlValues);

  return ({
    isDarkMode,
    dirent,
    id: state.id,
    name: fields.name,
    description: fields.description,
    dialobFormName: fields.formName,
    dialobFormTag: fields.formTag,
    flowName: fields.flowName,
    validityStart: fields.validityStart,
    validityEnd: fields.validityEnd,
    articles: fields.articles,
    configOptions: fields.configOptions,
    tagLabels: fields.tagLabels,
    intlValues: fields.intlValues,
    locales,
    isExpanded,
    isChanged: changes,
    onChangeName,
    onChangeDescription,
    onChangeDialobFormName,
    onChangeDialobFormTag,
    onChangeFlowName,
    onChangeValidityStart,
    onChangeValidityEnd,
    onChangeArticles,
    onChangeConfigOptions,
    onChangeLabels,
    onChangeIntlValues,
    onBlurName,
    onBlurDescription,
    onBlurIntlValues,
    onToggleExpanded,
    onCancel,
  });
};
