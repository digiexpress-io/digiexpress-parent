
import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface TextFields {
  value: string;
  intlValues: Record<string, string>;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  isExpanded: boolean;
  value: string;
  formName: string;
  formTag: string;
  flowName: string;
  articles: string[];
  intlValues: Record<string, string>;
  configOptions: Fs.ConfigOption[];
  tagLabels: string[];
  validityStart: string;
  validityEnd: string;
  locales: Fs.SelectOption[];
  onChangeValue: (v: string) => void;
  onBlurValue: () => void;
  onChangeFormName: (v: string) => void;
  onChangeFormTag: (v: string) => void;
  onChangeFlowName: (v: string) => void;
  onChangeArticles: (v: string[]) => void;
  onChangeIntlValues: (locale: string, value: string) => void;
  onBlurIntlValues: (locale: string) => void;
  onChangeConfigOptions: (v: string[]) => void;
  onChangeLabels: (v: string[]) => void;
  onChangeValidityStart: (date: Date | undefined) => void;
  onChangeValidityEnd: (date: Date | undefined) => void;
  onToggleExpanded: () => void;
  onSave: () => void;
  onCancel: () => void;
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
  tagLabels: string[];
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
  get tagLabels() { return this._current.tagLabels; }
  get validityStart() { return this._current.validityStart; }
  get validityEnd() { return this._current.validityEnd; }
  get isChanged(): boolean { return JSON.stringify(this._origin) !== JSON.stringify(this._current); }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        value: c.value,
        formName: c.formName,
        formTag: c.formTag,
        formId: c.formName,
        flowName: c.flowName,
        articles: c.articles,
        labels: Object.entries(c.intlValues).map(([locale, labelValue]) => ({ locale, labelValue })),
        startDate: c.validityStart || undefined,
        endDate: c.validityEnd || undefined,
        disabled: c.configOptions.includes('DISABLED_MODE') || undefined,
        devMode: c.configOptions.includes('DEV_MODE') || undefined,
        anon: c.configOptions.includes('ANONYMOUS_MODE') || undefined,
        assignable: c.configOptions.includes('ASSIGNABLE_MODE') || undefined,
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
  withTagLabels(tagLabels: string[]): _CreateState {
    return new _CreateState({ ...this._current, tagLabels }, this._origin);
  }
  withValidityStart(validityStart: string): _CreateState {
    return new _CreateState({ ...this._current, validityStart }, this._origin);
  }
  withValidityEnd(validityEnd: string): _CreateState {
    return new _CreateState({ ...this._current, validityEnd }, this._origin);
  }
}

const _initProps: _CreateStateProps = {
  bodyType: 'ARTICLE_WORKFLOW',
  value: '',
  formName: '',
  formTag: '',
  flowName: '',
  articles: [],
  intlValues: {},
  configOptions: [],
  tagLabels: [],
  validityStart: '',
  validityEnd: '',
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();
  const { selectOptions } = useFsDirent();

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [fields, setFields] = React.useState<TextFields>({ value: _initProps.value, intlValues: _initProps.intlValues });
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const isChangesPresent = state.isChanged
    || fields.value !== state.value
    || JSON.stringify(fields.intlValues) !== JSON.stringify(state.intlValues);

  function onChangeValue(v: string) {
    setFields(prev => ({ ...prev, value: v }));
  }
  function onBlurValue() {
    setState(prev => prev.withValue(fields.value));
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
    setFields(prev => ({ ...prev, intlValues: { ...prev.intlValues, [locale]: value } }));
  }
  function onBlurIntlValues(locale: string) {
    setState(prev => prev.withIntlValues(locale, fields.intlValues[locale] ?? ''));
  }
  function onChangeConfigOptions(v: string[]) {
    setState(prev => prev.withConfigOptions(v as Fs.ConfigOption[]));
  }
  function onChangeLabels(v: string[]) {
    setState(prev => prev.withTagLabels(v));
  }
  function onChangeValidityStart(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setState(prev => prev.withValidityStart(iso));
  }
  function onChangeValidityEnd(date: Date | undefined) {
    const iso = date ? date.toISOString() : '';
    setState(prev => prev.withValidityEnd(iso));
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
    setFields({ value: _initProps.value, intlValues: _initProps.intlValues });
    setStateRaw(new _CreateState(_initProps));
    setIsExpanded(false);
  }

  return ({
    isDarkMode,
    isChanged: isChangesPresent,
    isExpanded,
    value: fields.value,
    formName: state.formName,
    formTag: state.formTag,
    flowName: state.flowName,
    articles: state.articles,
    intlValues: fields.intlValues,
    configOptions: state.configOptions,
    tagLabels: state.tagLabels,
    validityStart: state.validityStart,
    validityEnd: state.validityEnd,
    locales: selectOptions.languages,
    onChangeValue,
    onBlurValue,
    onChangeFormName,
    onChangeFormTag,
    onChangeFlowName,
    onChangeArticles,
    onChangeIntlValues,
    onBlurIntlValues,
    onChangeConfigOptions,
    onChangeLabels,
    onChangeValidityStart,
    onChangeValidityEnd,
    onToggleExpanded,
    onSave,
    onCancel,
  });
};