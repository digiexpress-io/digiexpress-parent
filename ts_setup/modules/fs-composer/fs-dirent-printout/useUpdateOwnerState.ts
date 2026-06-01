import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


type _ChangeStateProps = {
  printoutId: string;
  bodyType: Fs.BodyType;
  serviceName: string;
  orchestratorName: string;
  intlValues: Record<string, string>;
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
  get bodyType() {
    return this._current.bodyType;
  }
  get serviceName() {
    return this._current.serviceName;
  }
  get orchestratorName() {
    return this._current.orchestratorName;
  }
  get intlValues() {
    return this._current.intlValues;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.printoutId,
      changes: {
        serviceId: c.printoutId,
        serviceName: c.serviceName || undefined,
        orchestratorName: c.orchestratorName || undefined,
        labels: Object.entries(c.intlValues).map(([locale, labelValue]) => ({ locale, labelValue })),
      },
    };
  }

  withServiceName(serviceName: string): _ChangeState {
    return new _ChangeState({ ...this._current, serviceName }, this._origin);
  }
  withOrchestratorName(orchestratorName: string): _ChangeState {
    return new _ChangeState({ ...this._current, orchestratorName }, this._origin);
  }
  withIntlValues(locale: string, labelValue: string): _ChangeState {
    return new _ChangeState({
      ...this._current,
      intlValues: { ...this._current.intlValues, [locale]: labelValue },
    }, this._origin);
  }
}

interface _TextFields {
  serviceName: string;
  intlValues: Record<string, string>;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  isExpanded: boolean;
  serviceName: string;
  orchestratorName: string;
  intlValues: Record<string, string>;
  locales: Fs.SelectOption[];
  flows: Fs.SelectOption[];
  onChangeServiceName: (v: string) => void;
  onBlurServiceName: () => void;
  onChangeOrchestratorName: (v: string) => void;
  onChangeIntlValues: (locale: string, value: string) => void;
  onBlurIntlValues: (locale: string) => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const printoutProps = dirent.props as Fs.PrintoutProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    printoutId: props.direntId,
    bodyType: dirent.type,
    serviceName: printoutProps.printoutServiceName ?? dirent.name ?? '',
    orchestratorName: printoutProps.orchestratorName ?? '',
    intlValues: printoutProps.intlValues ?? {},
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [fields, setFields] = React.useState<_TextFields>({
    serviceName: printoutProps.printoutServiceName ?? dirent.name ?? '',
    intlValues: printoutProps.intlValues ?? {},
  });

  const [isExpanded, setIsExpanded] = React.useState(false);

  const isChangesPresent = state.isChanged
    || fields.serviceName !== state.serviceName
    || JSON.stringify(fields.intlValues) !== JSON.stringify(state.intlValues);

  function onChangeServiceName(v: string) {
    setFields(prev => ({ ...prev, serviceName: v }));
  }
  function onBlurServiceName() {
    setState(prev => prev.withServiceName(fields.serviceName));
  }
  function onChangeOrchestratorName(v: string) {
    setState(prev => prev.withOrchestratorName(v));
  }
  function onChangeIntlValues(locale: string, value: string) {
    setFields(prev => ({ ...prev, intlValues: { ...prev.intlValues, [locale]: value } }));
  }
  function onBlurIntlValues(locale: string) {
    setState(prev => prev.withIntlValues(locale, fields.intlValues[locale] ?? ''));
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }
  function onCancel() {
    setFields({
      serviceName: printoutProps.printoutServiceName ?? dirent.name ?? '',
      intlValues: printoutProps.intlValues ?? {},
    });
    cancel(props.direntId);
    setIsExpanded(false);
  }

  return {
    isDarkMode,
    isChanged: isChangesPresent,
    isExpanded,
    serviceName: fields.serviceName,
    orchestratorName: state.orchestratorName,
    intlValues: fields.intlValues,
    locales: selectOptions.languages,
    flows: selectOptions.flows,
    onChangeServiceName,
    onBlurServiceName,
    onChangeOrchestratorName,
    onChangeIntlValues,
    onBlurIntlValues,
    onToggleExpanded,
    onCancel,
  };
};
