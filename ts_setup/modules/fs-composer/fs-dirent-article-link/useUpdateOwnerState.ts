import React from 'react';
import {
  Fs,
  useFsDirent,
  useFsuChange,
  FsuChange
} from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { createWidget } from '../fs-factory';

export type LocaleEntry = {
  localeCode: string;
  labelValue: string;
};

export interface UpdateOwnerState {
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  id: string;
  isDirty: boolean;
  contentType: Fs.LinkType;
  urlValue: string;
  intlValues: Record<string, string>;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  localeEntries: LocaleEntry[];
  isLocaleEntryIncomplete: boolean;
  onChangeLocaleEntry: (index: number, localeCode: string) => void;
  onChangeLabelEntry: (index: number, labelValue: string) => void;
  onChangeContentType: (value: string) => void;
  onChangeUrlValue: (value: string) => void;
  onChangeIntlValue: (locale: string, value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
}

type _ChangeStateProps = {
  linkId: string;
  bodyType: Fs.BodyType;
  type: Fs.LinkType;
  value: string;
  labels: { locale: string; labelValue: string }[];
  configOptions: Fs.ConfigOption[];
  articles: string[];
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.linkId; }
  get treeId() { return this._current.treeId; }
  get contentType() { return this._current.type; }
  get urlValue() { return this._current.value; }
  get intlValues() { return Object.fromEntries(this._current.labels.map(l => [l.locale, l.labelValue])); }
  get configOptions() { return this._current.configOptions; }
  get articles() { return this._current.articles; }

  getCurrentProps(): { bodyType: Fs.BodyType, id: string, changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: this.id,
      changes: {
        linkId: c.linkId,
        type: c.type,
        value: c.value,
        labels: c.labels,
        articles: c.articles,
        devMode: c.configOptions.includes('DEV_MODE') || undefined,
      }
    };
  }
  get bodyType() {
    return this._origin.bodyType;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }
  withContentType(contentType: Fs.LinkType): _ChangeState {
    return new _ChangeState({ ...this._current, type: contentType }, this._origin);
  }
  withUrlValue(urlValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, value: urlValue }, this._origin);
  }
  withIntlValue(locale: string, value: string): _ChangeState {
    const labels = this._current.labels.filter(l => l.locale !== locale);
    labels.push({ locale, labelValue: value });
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
  withArticles(articles: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, articles }, this._origin);
  }
  withConfigOptions(value: string[]): _ChangeState {
    const widget = createWidget({ type: 'ARTICLE_LINK' });
    return new _ChangeState({ ...this._current, configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)) }, this._origin);
  }
  withAllLabels(labels: { locale: string; labelValue: string }[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
}


export interface LinkArticleChange extends FsuChange {
  readonly articles: string[];
  withArticles(articles: string[]): LinkArticleChange;
}

export function createLinkFsuChange(dirent: Fs.DirentBase, articles: string[]): LinkArticleChange {
  const linkProps = dirent.props as Fs.LinkProps;
  const originProps: _ChangeStateProps = {
    linkId: dirent.id,
    bodyType: dirent.type,
    treeId: dirent.commitIndex?.treeId!,
    type: linkProps?.contentType ?? 'internal',
    value: linkProps?.urlValue ?? '',
    labels: Object.entries(linkProps?.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue })),
    configOptions: (linkProps?.configOptions ?? []) as Fs.ConfigOption[],
    articles: linkProps?.articles ?? [],
  };
  return new _ChangeState({ ...originProps, articles }, originProps);
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();
  const dirent = getDirent(props.direntId);
  const linkProps = dirent?.type === 'ARTICLE_LINK' ? dirent.props as Fs.LinkProps : undefined;

  const locales = selectOptions.languages;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    linkId: props.direntId,
    bodyType: dirent!.type,
    treeId: dirent?.commitIndex?.treeId!,
    type: linkProps?.contentType ?? 'internal',
    value: linkProps?.urlValue ?? '',
    labels: Object.entries(linkProps?.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue })),
    configOptions: (linkProps?.configOptions ?? []) as Fs.ConfigOption[],
    articles: linkProps?.articles ?? [],
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const [localeEntries, setLocaleEntries] = React.useState<LocaleEntry[]>(() => {
    const existingLabels = Object.entries(linkProps?.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue }));
    const existing = existingLabels.map((label) => ({
      localeCode: selectOptions.languages.find((lang) => lang.value === label.locale)?.label ?? label.locale,
      labelValue: label.labelValue,
    }));
    const usedCodes = existing.map((entry) => entry.localeCode);
    const remainingCodes = selectOptions.languages.map((lang) => lang.label).filter((code) => !usedCodes.includes(code));
    if (remainingCodes.length > 0) {
      existing.push({ localeCode: '', labelValue: '' });
    }
    return existing;
  });

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

  function onChangeLocaleEntry(index: number, localeCode: string) {
    const next = localeEntries.map((entry, entryIndex) =>
      entryIndex === index ? { localeCode, labelValue: localeCode === '' ? '' : entry.labelValue } : entry
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
    const next = localeEntries.map((entry, entryIndex) =>
      entryIndex === index ? { ...entry, labelValue } : entry
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

  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value as Fs.LinkType));
  }
  function onChangeUrlValue(value: string) {
    setState(prev => prev.withUrlValue(value)) 
  }
  function onChangeIntlValue(locale: string, value: string) {
    setState(prev => prev.withIntlValue(locale, value)) 
  }
  function onChangeArticles(value: string[]) {
    setState(prev => prev.withArticles(value));
  }
  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }



  return ({
    assetPath: activeTabPath,
    dirent,
    locales,
    id: state.id,
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
  });
};
