import React from 'react';
  import { useFsTheme } from '../fs-theme';
  import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
  import { useFsNav } from '@dxs-ts/fs-nav';


  interface _TextFields {
    serviceName: string;
    description: string;
  }

  export interface CreateOwnerState {
    isDarkMode: boolean;
    isChanged: boolean;
    serviceName: string;
    description: string;
    labels: string[];
    labelOptions: string[];
    orchestratorName: string;
    flows: Fs.SelectOption[];
    onChangeServiceName: (v: string) => void;
    onBlurServiceName: () => void;
    onChangeDescription: (v: string) => void;
    onBlurDescription: () => void;
    onChangeLabels: (value: string[]) => void;
    onChangeOrchestratorName: (v: string) => void;
    onSave: () => void;
    onCancel: () => void;
  }

  type _CreateStateProps = {
    bodyType: Fs.BodyType;
    serviceName: string;
    description: string;
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
    get description() {
      return this._current.description;
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
          description: c.description || undefined,
          tagLabels: c.labels.length ? c.labels : undefined,
          orchestratorName: c.orchestratorName || undefined,
          localeLabels: [],
        },
      };
    }

    withServiceName(serviceName: string): _CreateState {
      return new _CreateState({ ...this._current, serviceName }, this._origin);
    }
    withDescription(description: string): _CreateState {
      return new _CreateState({ ...this._current, description }, this._origin);
    }
    withLabels(labels: string[]): _CreateState {
      return new _CreateState({ ...this._current, labels }, this._origin);
    }
    withOrchestratorName(orchestratorName: string): _CreateState {
      return new _CreateState({ ...this._current, orchestratorName }, this._origin);
    }
  }

  const _initProps: _CreateStateProps = {
    bodyType: 'PRINTOUT',
    serviceName: '',
    description: '',
    labels: [],
    orchestratorName: '',
  };

  export const useCreateOwnerState = (): CreateOwnerState => {
    const { isDarkMode } = useFsTheme();
    const { pushCreate } = useFsu();
    const { openAsset } = useFsNav();
    const { selectOptions } = useFsDirent();

    const [fields, setFields] = React.useState<_TextFields>({
      serviceName: _initProps.serviceName,
      description: _initProps.description,
    });
    const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

    const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

    const isChangesPresent = state.isChanged
      || fields.serviceName !== state.serviceName
      || fields.description !== state.description;

    function onChangeServiceName(v: string) {
      setFields(prev => ({ ...prev, serviceName: v }));
    }
    function onBlurServiceName() {
      setState(prev => prev.withServiceName(fields.serviceName));
    }
    function onChangeDescription(v: string) {
      setFields(prev => ({ ...prev, description: v }));
    }
    function onBlurDescription() {
      setState(prev => prev.withDescription(fields.description));
    }
    function onChangeLabels(value: string[]) {
      setState(prev => prev.withLabels(value));
    }
    function onChangeOrchestratorName(v: string) {
      setState(prev => prev.withOrchestratorName(v));
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
      setFields({ serviceName: _initProps.serviceName, description: _initProps.description });
      setStateRaw(new _CreateState(_initProps));
    }

    return {
      isDarkMode,
      isChanged: isChangesPresent,
      serviceName: fields.serviceName,
      description: fields.description,
      labels: state.labels,
      labelOptions: selectOptions.labels,
      orchestratorName: state.orchestratorName,
      flows: selectOptions.flows,
      onChangeServiceName,
      onBlurServiceName,
      onChangeDescription,
      onBlurDescription,
      onChangeLabels,
      onChangeOrchestratorName,
      onSave,
      onCancel,
    };
  };
