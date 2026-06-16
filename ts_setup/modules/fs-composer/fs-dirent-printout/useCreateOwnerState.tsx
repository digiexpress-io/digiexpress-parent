import React from 'react';
  import { useFsTheme } from '../fs-theme';
  import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';


  export interface CreateOwnerState {
    isDarkMode: boolean;
    isDirty: boolean;
    serviceName: string;
    orchestratorName: string;
    flows: Fs.SelectOption[];
    onChangeServiceName: (value: string) => void;
    onChangeOrchestratorName: (value: string) => void;
  }

  type _CreateStateProps = {
    bodyType: Fs.BodyType;
    serviceName: string;
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
    get orchestratorName() {
      return this._current.orchestratorName;
    }
    get isDirty(): boolean {
      return JSON.stringify(this._origin) !== JSON.stringify(this._current);
    }

    getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
      const c = this._current;
      return {
        bodyType: c.bodyType,
        changes: {
          serviceName: c.serviceName || undefined,
          orchestratorName: c.orchestratorName || undefined,
          localeLabels: [],
        },
      };
    }

    withServiceName(serviceName: string): _CreateState {
      return new _CreateState({ ...this._current, serviceName }, this._origin);
    }
    withOrchestratorName(orchestratorName: string): _CreateState {
      return new _CreateState({ ...this._current, orchestratorName }, this._origin);
    }
  }

  const _init: _CreateStateProps = {
    bodyType: 'PRINTOUT',
    serviceName: '',
    orchestratorName: '',
  };

  export const useCreateOwnerState = (): CreateOwnerState => {
    const { isDarkMode } = useFsTheme();
    const { selectOptions } = useFsDirent();

    const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

    function onChangeServiceName(value: string) {
      setState(prev => prev.withServiceName(value));
    }
    function onChangeOrchestratorName(value: string) {
      setState(prev => prev.withOrchestratorName(value));
    }

    return {
      isDarkMode,
      isDirty: state.isDirty,
      serviceName: state.serviceName,
      orchestratorName: state.orchestratorName,
      flows: selectOptions.flows,
      onChangeServiceName,
      onChangeOrchestratorName,
    };
  };
