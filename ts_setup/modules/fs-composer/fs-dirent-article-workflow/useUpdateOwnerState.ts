import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


type _ChangeStateProps = {
  workflowId: string;
  bodyType: Fs.BodyType;
  value: string;
  assetDescription: { text: string };
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
  get assetDescription() { return this._current.assetDescription; }
  get intlValues() { return this._current.intlValues; }
  get validityStart() { return this._current.validityStart; }
  get validityEnd() { return this._current.validityEnd; }
  get flowName() { return this._current.flowName }
  get articles() { return this._current.articles }
  get formTag() { return this._current.formTag }
  get formName() { return this._current.formName }
  get tagLabels() { return this._current.tagLabels; }
  get configOptions() { return this._current.configOptions }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: this.id,
      changes: {
        workflowId: c.workflowId,
        value: c.value,
        assetDescription: c.assetDescription || undefined,
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

  withDescription(assetDescription: { text: string }): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
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


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  assetDescription: string;
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
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();
  const { withNewChange, withChange } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const workflowProps = dirent.props as Fs.WorkflowProps;
  const locales = selectOptions.languages;

  const [isExpanded, setIsExpanded] = React.useState(false);

  const state = withNewChange(props.direntId, () => new _ChangeState({
    workflowId: props.direntId,
    bodyType: dirent.type,
    value: dirent.name ?? '',
    assetDescription: { text: workflowProps.assetDescription ?? '' },
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
  const isChangesPresent = state.isChanged;

  function onChangeName(value: string) {
    setState(prev => prev.withFlowName(value));
  }
  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription({ text: value }));
  }
  function onChangeIntlValues(locale: string, value: string) {
    setState(prev => prev.withIntlValues(locale, value));
  }
  function onChangeDialobFormName(value: string) {
    setState(prev => prev.withFormName(value));
  }
  function onChangeDialobFormTag(value: string) {
    setState(prev => prev.withFormTag(value));
  }
  function onChangeFlowName(value: string) {
    setState(prev => prev.withFlowName(value));
  }
  function onChangeValidityStart(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setState(prev => prev.withValidityStart(iso));
  }
  function onChangeValidityEnd(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setState(prev => prev.withValidityEnd(iso));
  }
  function onChangeArticles(value: string[]) {
    setState(prev => prev.withArticles(value));
  }
  function onChangeConfigOptions(value: string[]) {
    const opts = value as Fs.ConfigOption[];
    setState(prev => prev.withConfigOptions(opts));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withTagLabels(value));
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }
  return ({
    isDarkMode,
    dirent,
    id: state.id,
    assetDescription: state.assetDescription.text,
    dialobFormName: state.formName,
    dialobFormTag: state.formTag,
    flowName: state.flowName,
    validityStart: state.validityStart,
    validityEnd: state.validityEnd,
    articles: state.articles,
    configOptions: state.configOptions,
    tagLabels: state.tagLabels,
    intlValues: state.intlValues,
    locales,
    isExpanded,
    isChanged: isChangesPresent,
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
    onToggleExpanded,
  });
};
