import React from 'react';

  import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
    isDirty: boolean;
    serviceName: string;
    orchestratorName: string;
    flows: Fs.SelectOption[];
    locales: Fs.SelectOption[];
    intlValues: Record<string, string>;
    onChangeServiceName: (value: string) => void;
    onChangeOrchestratorName: (value: string) => void;
    onChangeIntlValue: (locale: string, value: string) => void;
    onSave: () => Promise<void>;
  }

  type _CreateStateProps = {
    bodyType: Fs.BodyType;
    serviceName: string;
    orchestratorName: string;
    localeLabels: { locale: string; labelValue: string }[];
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
    get intlValues() {
      return Object.fromEntries(this._current.localeLabels.map(l => [l.locale, l.labelValue]));
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
          localeLabels: c.localeLabels,
        },
      };
    }

    withServiceName(serviceName: string): _CreateState {
      return new _CreateState({ ...this._current, serviceName }, this._origin);
    }
    withOrchestratorName(orchestratorName: string): _CreateState {
      return new _CreateState({ ...this._current, orchestratorName }, this._origin);
    }
    withIntlValue(locale: string, labelValue: string): _CreateState {
      const localeLabels = this._current.localeLabels.filter(l => l.locale !== locale);
      localeLabels.push({ locale, labelValue });
      return new _CreateState({ ...this._current, localeLabels }, this._origin);
    }
  }

  const _init: _CreateStateProps = {
    bodyType: 'PRINTOUT',
    serviceName: '',
    orchestratorName: '',
    localeLabels: [],
  };

export const useCreateOwnerState = (): CreateOwnerState => {
    const { selectOptions, createDirent } = useFsDirent();

    const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

    function onChangeServiceName(value: string) {
      setState(prev => prev.withServiceName(value));
    }
    function onChangeOrchestratorName(value: string) {
      setState(prev => prev.withOrchestratorName(value));
    }
    function onChangeIntlValue(locale: string, value: string) {
      setState(prev => prev.withIntlValue(locale, value));
    }
    async function onSave() {
      await createDirent(state);
    }

    return {
      isDirty: state.isDirty,
      serviceName: state.serviceName,
      orchestratorName: state.orchestratorName,
      flows: selectOptions.flows,
      locales: selectOptions.languages,
      intlValues: state.intlValues,
      onChangeServiceName,
      onChangeOrchestratorName,
      onChangeIntlValue,
      onSave,
    };
  };
