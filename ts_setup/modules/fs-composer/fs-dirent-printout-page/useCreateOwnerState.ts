import React from 'react';
import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';

interface _TextFields {
  content: string;
  assetDescription: { text: string };
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  isExpanded: boolean;
  serviceId: string;
  localeId: string;
  content: string;
  assetDescription: { text: string };
  labels: string[];
  labelOptions: string[];
  printoutOptions: Fs.SelectOption[];
  localeOptions: Fs.SelectOption[];
  onChangeServiceId: (value: string) => void;
  onChangeLocaleId: (value: string) => void;
  onChangeContent: (value: string) => void;
  onBlurContent: () => void;
  onChangeDescription: (value: string) => void;
  onBlurDescription: () => void;
  onChangeLabels: (value: string[]) => void;
  onToggleExpanded: () => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  serviceId: string;
  localeId: string;
  content: string;
  assetDescription: { text: string };
  labels: string[];
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
  get serviceId() {
    return this._current.serviceId;
  }
  get localeId() {
    return this._current.localeId;
  }
  get content() {
    return this._current.content;
  }
  get assetDescription() {
    return this._current.assetDescription;
  }
  get labels() {
    return this._current.labels;
  }
  get isChanged(): boolean {
    return !!this._current.serviceId && !!this._current.localeId;
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        serviceId: this._current.serviceId,
        localeId: this._current.localeId,
        content: this._current.content || undefined,
        assetDescription: this._current.assetDescription || undefined,
        labels: this._current.labels.length ? this._current.labels : undefined,
      }
    };
  }

  withServiceId(serviceId: string): _CreateState {
    return new _CreateState({ ...this._current, serviceId, localeId: '' }, this._origin);
  }
  withLocaleId(localeId: string): _CreateState {
    return new _CreateState({ ...this._current, localeId }, this._origin);
  }
  withContent(content: string): _CreateState {
    return new _CreateState({ ...this._current, content }, this._origin);
  }
  withDescription(assetDescription: { text: string }): _CreateState {
    return new _CreateState({ ...this._current, assetDescription }, this._origin);
  }
  withLabels(labels: string[]): _CreateState {
    return new _CreateState({ ...this._current, labels }, this._origin);
  }
}

const _initFields: _TextFields = { content: '', assetDescription: { text: '' } };

const _initProps: _CreateStateProps = {
  bodyType: 'PRINTOUT_PAGE',
  serviceId: '',
  localeId: '',
  content: '',
  assetDescription: { text: '' },
  labels: [],
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions } = useFsDirent();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [fields, setFields] = React.useState<_TextFields>(_initFields);
  const [isExpanded, setIsExpanded] = React.useState(false);
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const usedLocaleIds = state.serviceId
    ? Object.values(selectOptions.direntProps)
      .filter(p => p.type === 'PRINTOUT_PAGE' && (p as Fs.PrintoutPageProps).serviceId === state.serviceId)
      .map(p => (p as Fs.PrintoutPageProps).localeId)
    : [];

  const localeOptions: Fs.SelectOption[] = selectOptions.languages.filter(
    l => !usedLocaleIds.includes(l.value)
  );

  function onChangeServiceId(value: string) {
    setState(prev => prev.withServiceId(value));
  }
  function onChangeLocaleId(value: string) {
    setState(prev => prev.withLocaleId(value));
  }
  function onChangeContent(value: string) {
    setFields(prev => ({ ...prev, content: value }));
  }
  function onBlurContent() {
    setState(prev => prev.withContent(fields.content));
  }
  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, assetDescription: { text: value } }));
  }
  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.assetDescription));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
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
    setFields(_initFields);
    setIsExpanded(false);
    setStateRaw(new _CreateState(_initProps));
  }

  return {
    isDarkMode,
    isChanged: state.isChanged,
    isExpanded,
    serviceId: state.serviceId,
    localeId: state.localeId,
    content: fields.content,
    assetDescription: fields.assetDescription,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    printoutOptions: selectOptions.printouts,
    localeOptions,
    onChangeServiceId,
    onChangeLocaleId,
    onChangeContent,
    onBlurContent,
    onChangeDescription,
    onBlurDescription,
    onChangeLabels,
    onToggleExpanded,
    onSave,
    onCancel,
  };
};
