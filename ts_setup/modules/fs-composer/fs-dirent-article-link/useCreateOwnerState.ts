import React from 'react';
import {
  Fs,
  useFsDirent,
  FsuCreateChange,
} from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { createWidget } from '../fs-factory';

export type LocaleEntry = {
  localeCode: string;
  labelValue: string;
};

export interface CreateOwnerState {
  locales: Fs.SelectOption[];
  isDirty: boolean;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  localeEntries: LocaleEntry[];
  isLocaleEntryIncomplete: boolean;
  onChangeContentType: (value: string) => void;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLocaleEntry: (index: number, localeCode: string) => void;
  onChangeLabelEntry: (index: number, labelValue: string) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  type: Fs.LinkType;
  value: string;
  labels: { locale: string; labelValue: string }[];
  configOptions: Fs.ConfigOption[];
  articles: string[];
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() {
    return this._current.bodyType;
  }
  get contentType() {
    return this._current.type;
  }
  get urlValue() {
    return this._current.value;
  }
  get intlValues() {
    return Object.fromEntries(this._current.labels.map(entry => [entry.locale, entry.labelValue]));
  }
  get configOptions() {
    return this._current.configOptions;
  }
  get articles() {
    return this._current.articles;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        value: c.value,
        type: c.type,
        labels: c.labels,
        articles: c.articles,
        devMode: c.configOptions.includes('DEV_MODE') || undefined,
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
    const labels = this._current.labels.filter(label => label.locale !== locale);
    labels.push({ locale, labelValue: value });
    return new _CreateState({ ...this._current, labels }, this._origin);
  }
  withArticles(articles: string[]): _CreateState {
    return new _CreateState({ ...this._current, articles }, this._origin);
  }
  withConfigOptions(value: string[]): _CreateState {
    const widget = createWidget({ type: 'ARTICLE_LINK' });
    return new _CreateState({ ...this._current, configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)) }, this._origin);
  }
  withAllLabels(labels: { locale: string; labelValue: string }[]): _CreateState {
    return new _CreateState({ ...this._current, labels }, this._origin);
  }
}

const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_LINK',
  type: 'internal',
  value: '',
  labels: [],
  configOptions: [],
  articles: [],
};


export const useCreateOwnerState = (): CreateOwnerState => {
  const { selectOptions, createDirent } = useFsDirent();
  const { openAsset } = useFsNav();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));
  const [localeEntries, setLocaleEntries] = React.useState<LocaleEntry[]>([{ localeCode: '', labelValue: '' }]);
  const locales = selectOptions.languages;

  const isLocaleEntryIncomplete = localeEntries.some(
    (entry) => entry.localeCode !== '' && entry.labelValue === ''
  );

  function syncLabelsToState(entries: LocaleEntry[]) {
    const completedLabels = entries
      .filter((entry) => entry.localeCode !== '' && entry.labelValue !== '')
      .map((entry) => {
        const direntId = selectOptions.languages.find((lang) => lang.label === entry.localeCode)?.value ?? entry.localeCode;
        return { locale: direntId, labelValue: entry.labelValue };
      });
    setState((prev) => prev.withAllLabels(completedLabels));
  }

  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value as Fs.LinkType));
  }
  function onChangeUrlValue(value: string) {
    setState(prev => prev.withUrlValue(value));
  }
  function onChangeIntlValue(locale: string, value: string) {
    setState(prev => prev.withIntlValue(locale, value));
  }
  function onChangeArticles(value: string[]) {
    setState(prev => prev.withArticles(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onChangeLocaleEntry(index: number, localeCode: string) {
    const next = localeEntries.map((entry, i) =>
      i === index ? { localeCode, labelValue: localeCode === '' ? '' : entry.labelValue } : entry
    );
    const last = next[next.length - 1];
    if (last.localeCode !== '' && last.labelValue !== '') {
      const usedCodes = next.filter((entry) => entry.localeCode !== '').map((entry) => entry.localeCode);
      const remainingCodes = selectOptions.languages.map((lang) => lang.label).filter((code) => !usedCodes.includes(code));
      if (remainingCodes.length > 0) {
        next.push({ localeCode: '', labelValue: '' });
      }
    }
    setLocaleEntries(next);
    syncLabelsToState(next);
  }
  function onChangeLabelEntry(index: number, labelValue: string) {
    const next = localeEntries.map((entry, i) =>
      i === index ? { ...entry, labelValue } : entry
    );
    const last = next[next.length - 1];
    if (last.localeCode !== '' && last.labelValue !== '') {
      const usedCodes = next.filter((entry) => entry.localeCode !== '').map((entry) => entry.localeCode);
      const remainingCodes = selectOptions.languages.map((lang) => lang.label).filter((code) => !usedCodes.includes(code));
      if (remainingCodes.length > 0) {
        next.push({ localeCode: '', labelValue: '' });
      }
    }
    setLocaleEntries(next);
    syncLabelsToState(next);
  }
  async function onSave() {
    const newDirent = await createDirent(state);
    openAsset(newDirent);
  }

  return ({
    locales,
    isDirty: state.isDirty,
    contentType: state.contentType,
    urlValue: state.urlValue,
    intlValues: state.intlValues,
    articles: state.articles,
    configOptions: state.configOptions,
    localeEntries,
    isLocaleEntryIncomplete,
    onChangeContentType,
    onChangeUrlValue,
    onChangeIntlValue,
    onChangeArticles,
    onChangeConfigOptions,
    onChangeLocaleEntry,
    onChangeLabelEntry,
    onSave,
  });
};
