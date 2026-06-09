import React from 'react';
  import { useFsTheme } from '../fs-theme';
  import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';


  export interface CreateOwnerState {
    isDarkMode: boolean;
    isChanged: boolean;
    serviceName: string;
    assetDescription: string;
    labels: string[];
    labelOptions: string[];
    orchestratorName: string;
    flows: Fs.SelectOption[];
    onChangeServiceName: (value: string) => void;
    onChangeDescription: (value: string) => void;
    onChangeLabels: (value: string[]) => void;
    onChangeOrchestratorName: (value: string) => void;
  }

  type _CreateStateProps = {
    bodyType: Fs.BodyType;
    serviceName: string;
    assetDescription: { text: string };
    labels: string[];
    orchestratorName: string;
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
    get assetDescription() {
      return this._current.assetDescription;
    }
    get labels() {
      return this._current.labels;
    }
    get orchestratorName() {
      return this._current.orchestratorName;
    }
    get isChanged(): boolean {
      return JSON.stringify(this._origin) !== JSON.stringify(this._current);
    }

    getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
      const c = this._current;
      return {
        bodyType: c.bodyType,
        changes: {
          serviceName: c.serviceName || undefined,
          assetDescription: c.assetDescription || undefined,
          tagLabels: c.labels.length ? c.labels : undefined,
          orchestratorName: c.orchestratorName || undefined,
          localeLabels: [],
        },
      };
    }

    withServiceName(serviceName: string): _CreateState {
      return new _CreateState({ ...this._current, serviceName }, this._origin);
    }
    withDescription(assetDescription: { text: string }): _CreateState {
      return new _CreateState({ ...this._current, assetDescription }, this._origin);
    }
    withLabels(labels: string[]): _CreateState {
      return new _CreateState({ ...this._current, labels }, this._origin);
    }
    withOrchestratorName(orchestratorName: string): _CreateState {
      return new _CreateState({ ...this._current, orchestratorName }, this._origin);
    }
  }

  const _init: _CreateStateProps = {
    bodyType: 'PRINTOUT',
    serviceName: '',
    assetDescription: { text: '' },
    labels: [],
    orchestratorName: '',
  };

  export const useCreateOwnerState = (): CreateOwnerState => {
    const { isDarkMode } = useFsTheme();
    const { selectOptions } = useFsDirent();

    const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

    function onChangeServiceName(value: string) {
      setState(prev => prev.withServiceName(value));
    }
    function onChangeDescription(value: string) {
      setState(prev => prev.withDescription({ text: value }));
    }
    function onChangeLabels(value: string[]) {
      setState(prev => prev.withLabels(value));
    }
    function onChangeOrchestratorName(value: string) {
      setState(prev => prev.withOrchestratorName(value));
    }

    return {
      isDarkMode,
      isChanged: state.isChanged,
      serviceName: state.serviceName,
      assetDescription: state.assetDescription.text,
      labels: state.labels,
      labelOptions: selectOptions.labels,
      orchestratorName: state.orchestratorName,
      flows: selectOptions.flows,
      onChangeServiceName,
      onChangeDescription,
      onChangeLabels,
      onChangeOrchestratorName,
    };
  };
