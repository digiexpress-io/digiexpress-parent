import React from 'react';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

export type LocaleEntry = {
  localeCode: string;
  labelValue: string;
};

type _CreateChanges = {
  serviceName: string | undefined;
  orchestratorName: string | undefined;
  localeLabels: { locale: string; labelValue: string }[];
};

export interface CreateOwnerState {
  isDirty: boolean;
  isLocaleEntryIncomplete: boolean;
  serviceName: string;
  orchestratorName: string;
  flows: Fs.SelectOption[];
  localeEntries: LocaleEntry[];
  availableCodes: (index: number) => string[];
  onChangeServiceName: (value: string) => void;
  onChangeOrchestratorName: (value: string) => void;
  onChangeLocaleEntry: (index: number, localeCode: string) => void;
  onChangeLabelEntry: (index: number, labelValue: string) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  serviceName: string;
  orchestratorName: string;
  localeEntries: LocaleEntry[];
  languages: Fs.SelectOption[];
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
  get serviceName() {
    return this._current.serviceName;
  }
  get orchestratorName() {
    return this._current.orchestratorName;
  }
  get localeEntries() {
    return this._current.localeEntries;
  }
  get languages() {
    return this._current.languages;
  }
  get isDirty(): boolean {
    const { languages: _a, ...originRest } = this._origin;
    const { languages: _b, ...currentRest } = this._current;
    return JSON.stringify(originRest) !== JSON.stringify(currentRest);
  }

  private get _localeLabels(): { locale: string; labelValue: string }[] {
    return this._current.localeEntries
      .filter((entry) => entry.localeCode !== '' && entry.labelValue !== '')
      .map((entry) => {
        const localeId = this._current.languages.find((lang) => lang.label === entry.localeCode)?.value ?? entry.localeCode;
        return { locale: localeId, labelValue: entry.labelValue };
      });
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: _CreateChanges } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        serviceName: c.serviceName || undefined,
        orchestratorName: c.orchestratorName || undefined,
        localeLabels: this._localeLabels,
      },
    };
  }

  withServiceName(serviceName: string): _CreateState {
    return new _CreateState({ ...this._current, serviceName }, this._origin);
  }
  withOrchestratorName(orchestratorName: string): _CreateState {
    return new _CreateState({ ...this._current, orchestratorName }, this._origin);
  }
  withLocaleEntries(localeEntries: LocaleEntry[]): _CreateState {
    return new _CreateState({ ...this._current, localeEntries }, this._origin);
  }
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { selectOptions, createDirent } = useFsDirent();
  const { openAsset } = useFsNav();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState({
    bodyType: 'PRINTOUT',
    serviceName: '',
    orchestratorName: '',
    localeEntries: [{ localeCode: '', labelValue: '' }],
    languages: selectOptions.languages,
  }));

  const isLocaleEntryIncomplete = state.localeEntries.some(
    (entry) => entry.localeCode !== '' && entry.labelValue === ''
  );

  function availableCodes(index: number): string[] {
    const usedCodes = state.localeEntries
      .filter((_otherEntry, otherIndex) => otherIndex !== index && _otherEntry.localeCode !== '')
      .map((otherEntry) => otherEntry.localeCode);
    return selectOptions.languages
      .map((lang) => lang.label)
      .filter((code) => !usedCodes.includes(code));
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

  function onChangeServiceName(value: string) {
    setState(prev => prev.withServiceName(value));
  }
  function onChangeOrchestratorName(value: string) {
    setState(prev => prev.withOrchestratorName(value));
  }
  function onChangeLocaleEntry(index: number, localeCode: string) {
    const next = state.localeEntries.map((entry, i) =>
      i === index ? { localeCode, labelValue: localeCode === '' ? '' : entry.labelValue } : entry
    );
    setState(prev => prev.withLocaleEntries(_withAutoAppend(next)));
  }
  function onChangeLabelEntry(index: number, labelValue: string) {
    const next = state.localeEntries.map((entry, i) =>
      i === index ? { ...entry, labelValue } : entry
    );
    setState(prev => prev.withLocaleEntries(_withAutoAppend(next)));
  }
  async function onSave() {
    const newDirent = await createDirent(state);
    openAsset(newDirent);
  }

  return {
    isDirty: state.isDirty,
    isLocaleEntryIncomplete,
    serviceName: state.serviceName,
    orchestratorName: state.orchestratorName,
    flows: selectOptions.flows,
    localeEntries: state.localeEntries,
    availableCodes,
    onChangeServiceName,
    onChangeOrchestratorName,
    onChangeLocaleEntry,
    onChangeLabelEntry,
    onSave,
  };
};
