import React from 'react';
import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export type LocaleEntry = {
  localeCode: string;
  labelValue: string;
};

export interface ConnectedPage {
  id: string;
  localeName: string;
}

type _ChangeStateProps = {
  printoutId: string;
  bodyType: Fs.BodyType;
  serviceName: string;
  orchestratorName: string;
  labels: { locale: string; labelValue: string }[];
  treeId: string;
}

type _ChangeChanges = {
  serviceId: string;
  serviceName: string | undefined;
  orchestratorName: string | undefined;
  labels: { locale: string; labelValue: string }[];
};

export interface UpdateOwnerState {
  assetPath: string | undefined;
  isDirty: boolean;
  isLocaleEntryIncomplete: boolean;
  serviceName: string;
  orchestratorName: string;
  flows: Fs.SelectOption[];
  localeEntries: LocaleEntry[];
  connectedPages: ConnectedPage[];
  availableCodes: (index: number) => string[];
  onChangeServiceName: (value: string) => void;
  onChangeOrchestratorName: (value: string) => void;
  onChangeLocaleEntry: (index: number, localeCode: string) => void;
  onChangeLabelEntry: (index: number, labelValue: string) => void;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.printoutId;
  }
  get treeId() { return this._current.treeId; }
  get bodyType() {
    return this._current.bodyType;
  }
  get serviceName() {
    return this._current.serviceName;
  }
  get orchestratorName() {
    return this._current.orchestratorName;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: _ChangeChanges } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.printoutId,
      changes: {
        serviceId: c.printoutId,
        serviceName: c.serviceName || undefined,
        orchestratorName: c.orchestratorName || undefined,
        labels: c.labels,
      },
    };
  }

  withServiceName(serviceName: string): _ChangeState {
    return new _ChangeState({ ...this._current, serviceName }, this._origin);
  }
  withOrchestratorName(orchestratorName: string): _ChangeState {
    return new _ChangeState({ ...this._current, orchestratorName }, this._origin);
  }
  withAllLabels(labels: { locale: string; labelValue: string }[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const printoutProps = (dirent?.props ?? {}) as Fs.PrintoutProps;
  const treeId = dirent?.commitIndex?.treeId ?? '';

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    printoutId: props.direntId,
    bodyType: dirent?.type ?? 'PRINTOUT',
    treeId,
    serviceName: printoutProps.printoutServiceName ?? dirent?.name ?? '',
    orchestratorName: printoutProps.orchestratorName ?? '',
    labels: Object.entries(printoutProps.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue })),
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const [localeEntries, setLocaleEntries] = React.useState<LocaleEntry[]>(() => {
    const existingLabels = Object.entries(printoutProps.intlValues ?? {}).map(([locale, labelValue]) => ({ locale, labelValue }));
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

  function availableCodes(index: number): string[] {
    const usedCodes = localeEntries
      .filter((_otherEntry, otherIndex) => otherIndex !== index && _otherEntry.localeCode !== '')
      .map((otherEntry) => otherEntry.localeCode);
    return selectOptions.languages
      .map((lang) => lang.label)
      .filter((code) => !usedCodes.includes(code));
  }

  function syncLabelsToState(entries: LocaleEntry[]) {
    const completedLabels = entries
      .filter((entry) => entry.localeCode !== '' && entry.labelValue !== '')
      .map((entry) => {
        const direntId = selectOptions.languages.find((lang) => lang.label === entry.localeCode)?.value ?? entry.localeCode;
        return { locale: direntId, labelValue: entry.labelValue };
      });
    setState((prev) => prev.withAllLabels(completedLabels));
  }

  function _withAutoAppend(next: LocaleEntry[]): LocaleEntry[] {
    const last = next[next.length - 1];
    if (last && last.localeCode !== '' && last.labelValue !== '') {
      const usedCodes = next.filter((entry) => entry.localeCode !== '').map((entry) => entry.localeCode);
      const remainingCodes = selectOptions.languages.map((lang) => lang.label).filter((code) => !usedCodes.includes(code));
      if (remainingCodes.length > 0) {
        return [...next, { localeCode: '', labelValue: '' }];
      }
    }
    return next;
  }

  function onChangeLocaleEntry(index: number, localeCode: string) {
    const next = localeEntries.map((entry, entryIndex) =>
      entryIndex === index ? { localeCode, labelValue: localeCode === '' ? '' : entry.labelValue } : entry
    );
    const appended = _withAutoAppend(next);
    setLocaleEntries(appended);
    syncLabelsToState(appended);
  }

  function onChangeLabelEntry(index: number, labelValue: string) {
    const next = localeEntries.map((entry, entryIndex) =>
      entryIndex === index ? { ...entry, labelValue } : entry
    );
    const appended = _withAutoAppend(next);
    setLocaleEntries(appended);
    syncLabelsToState(appended);
  }

  function onChangeServiceName(value: string) {
    setState(prev => prev.withServiceName(value));
  }
  function onChangeOrchestratorName(value: string) {
    setState(prev => prev.withOrchestratorName(value));
  }

  const connectedPages: ConnectedPage[] = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_PAGE' && (p as Fs.PrintoutPageProps).serviceId === props.direntId)
    .map(p => {
      const pageProps = p as Fs.PrintoutPageProps;
      const localeName = selectOptions.languages.find((lang) => lang.value === pageProps.localeId)?.label ?? pageProps.localeId;
      return { id: p.id, localeName };
    });

  return {
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    isLocaleEntryIncomplete,
    serviceName: state.serviceName,
    orchestratorName: state.orchestratorName,
    flows: selectOptions.flows,
    localeEntries,
    connectedPages,
    availableCodes,
    onChangeServiceName,
    onChangeOrchestratorName,
    onChangeLocaleEntry,
    onChangeLabelEntry,
  };
};
