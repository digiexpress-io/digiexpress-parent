
import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';

export interface CreateOwnerState {
  isDarkMode: boolean;
  isDirty: boolean;
  value: string;
  formName: string;
  formTag: string;
  flowName: string;
  articles: string[];
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  validityStart: string;
  validityEnd: string;
  locales: Fs.SelectOption[];
  onChangeValue: (v: string) => void;
  onChangeFormName: (v: string) => void;
  onChangeFormTag: (v: string) => void;
  onChangeFlowName: (v: string) => void;
  onChangeArticles: (v: string[]) => void;
  onChangeIntlValues: (locale: string, value: string) => void;
  onChangeConfigOptions: (v: string[]) => void;
  onChangeValidityStart: (date: Date | undefined) => void;
  onChangeValidityEnd: (date: Date | undefined) => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  value: string;
  formName: string;
  formTag: string;
  flowName: string;
  articles: string[];
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  validityStart: string;
  validityEnd: string;
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get value() { return this._current.value; }
  get formName() { return this._current.formName; }
  get formTag() { return this._current.formTag; }
  get flowName() { return this._current.flowName; }
  get articles() { return this._current.articles; }
  get intlValues() { return this._current.intlValues; }
  get configOptions() { return this._current.configOptions; }
  get validityStart() { return this._current.validityStart; }
  get validityEnd() { return this._current.validityEnd; }
  get isDirty(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const current = this._current;
    return {
      bodyType: current.bodyType,
      changes: {
        value: current.value,
        formName: current.formName,
        formTag: current.formTag,
        formId: current.formName,
        flowName: current.flowName,
        articles: current.articles,
        startDate: current.validityStart || undefined,
        endDate: current.validityEnd || undefined,
        disabled: current.configOptions.includes('DISABLED_MODE') || undefined,
        devMode: current.configOptions.includes('DEV_MODE') || undefined,
        anon: current.configOptions.includes('ANONYMOUS_MODE') || undefined,
        assignable: current.configOptions.includes('ASSIGNABLE_MODE') || undefined,
      },
    };
  }

  withValue(value: string): _CreateState {
    return new _CreateState({ ...this._current, value }, this._origin);
  }
  withFormName(formName: string): _CreateState {
    return new _CreateState({ ...this._current, formName, formTag: '' }, this._origin);
  }
  withFormTag(formTag: string): _CreateState {
    return new _CreateState({ ...this._current, formTag }, this._origin);
  }
  withFlowName(flowName: string): _CreateState {
    return new _CreateState({ ...this._current, flowName }, this._origin);
  }
  withArticles(articles: string[]): _CreateState {
    return new _CreateState({ ...this._current, articles }, this._origin);
  }
  withIntlValues(locale: string, labelValue: string): _CreateState {
    return new _CreateState({ ...this._current, intlValues: { ...this._current.intlValues, [locale]: labelValue } }, this._origin);
  }
  withConfigOptions(configOptions: Fs.ConfigOption[]): _CreateState {
    return new _CreateState({ ...this._current, configOptions }, this._origin);
  }
  withValidityStart(validityStart: string): _CreateState {
    return new _CreateState({ ...this._current, validityStart }, this._origin);
  }
  withValidityEnd(validityEnd: string): _CreateState {
    return new _CreateState({ ...this._current, validityEnd }, this._origin);
  }
}

const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_WORKFLOW',
  value: '',
  formName: '',
  formTag: '',
  flowName: '',
  articles: [],
  intlValues: {},
  configOptions: [],
  validityStart: '',
  validityEnd: '',
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions } = useFsDirent();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  const isChangesPresent = state.isDirty;

  function onChangeValue(value: string) {
    setState(prev => prev.withValue(value));
  }
  function onChangeFormName(v: string) {
    setState(prev => prev.withFormName(v));
  }
  function onChangeFormTag(v: string) {
    setState(prev => prev.withFormTag(v));
  }
  function onChangeFlowName(v: string) {
    setState(prev => prev.withFlowName(v));
  }
  function onChangeArticles(v: string[]) {
    setState(prev => prev.withArticles(v));
  }
  function onChangeIntlValues(locale: string, value: string) {
    setState(prev => prev.withIntlValues(locale, value));
  }
  function onChangeConfigOptions(values: string[]) {
    setState(prev => prev.withConfigOptions(values as Fs.ConfigOption[]));
  }
  function onChangeValidityStart(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setState(prev => prev.withValidityStart(iso));
  }
  function onChangeValidityEnd(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setState(prev => prev.withValidityEnd(iso));
  }

  return ({
    isDarkMode,
    isDirty: isChangesPresent,
    value: state.value,
    formName: state.formName,
    formTag: state.formTag,
    flowName: state.flowName,
    articles: state.articles,
    intlValues: state.intlValues,
    configOptions: state.configOptions,
    validityStart: state.validityStart,
    validityEnd: state.validityEnd,
    locales: selectOptions.languages,
    onChangeValue,
    onChangeFormName,
    onChangeFormTag,
    onChangeFlowName,
    onChangeArticles,
    onChangeIntlValues,
    onChangeConfigOptions,
    onChangeValidityStart,
    onChangeValidityEnd,
  });
};