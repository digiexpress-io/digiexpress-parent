import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';

export interface ConnectedPage {
  id: string;
  localeName: string;
}


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
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  serviceName: string;
  orchestratorName: string;
  flows: Fs.SelectOption[];
  connectedPages: ConnectedPage[];
  onChangeServiceName: (v: string) => void;
  onBlurServiceName: () => void;
  onChangeOrchestratorName: (v: string) => void;
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
  });

  const isChangesPresent = state.isChanged || fields.serviceName !== state.serviceName;

  const connectedPages: ConnectedPage[] = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_PAGE' && (p as Fs.PrintoutPageProps).serviceId === props.direntId)
    .map(p => {
      const pageProps = p as Fs.PrintoutPageProps;
      const localeName = selectOptions.languages.find(l => l.value === pageProps.localeId)?.label ?? pageProps.localeId;
      return { id: p.id, localeName };
    });

  function onChangeServiceName(v: string) {
    setFields(prev => ({ ...prev, serviceName: v }));
  }
  function onBlurServiceName() {
    setState(prev => prev.withServiceName(fields.serviceName));
  }
  function onChangeOrchestratorName(v: string) {
    setState(prev => prev.withOrchestratorName(v));
  }
  function onCancel() {
    setFields({ serviceName: printoutProps.printoutServiceName ?? dirent.name ?? '' });
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    isChanged: isChangesPresent,
    serviceName: fields.serviceName,
    orchestratorName: state.orchestratorName,
    flows: selectOptions.flows,
    connectedPages,
    onChangeServiceName,
    onBlurServiceName,
    onChangeOrchestratorName,
    onCancel,
  };
};
