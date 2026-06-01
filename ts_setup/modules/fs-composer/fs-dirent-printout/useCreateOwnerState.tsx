import React from 'react';
  import { useFsTheme } from '../fs-theme';
  import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
  import { useFsNav } from '@dxs-ts/fs-nav';


  export interface TextFields {
    serviceName: string;
    intlValues: Record<string, string>;
  }

  export interface CreateOwnerState {
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
    onSave: () => void;
    onCancel: () => void;
  }

  type _CreateStateProps = {
    bodyType: Fs.BodyType;
    serviceName: string;
    orchestratorName: string;
    intlValues: Record<string, string>;
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
      return this._current.intlValues;
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
          orchestratorName: c.orchestratorName || undefined,
          labels: Object.entries(c.intlValues).map(([locale, labelValue]) => ({ locale, labelValue })),
        },
      };
    }

    withServiceName(serviceName: string): _CreateState {
      return new _CreateState({ ...this._current, serviceName }, this._origin);
    }
    withOrchestratorName(orchestratorName: string): _CreateState {
      return new _CreateState({ ...this._current, orchestratorName }, this._origin);
    }
    withIntlValues(locale: string, labelValue: string): _CreateState {
      return new _CreateState({
        ...this._current,
        intlValues: { ...this._current.intlValues, [locale]: labelValue },
      }, this._origin);
    }
  }

  const _initProps: _CreateStateProps = {
    bodyType: 'PRINTOUT',
    serviceName: '',
    orchestratorName: '',
    intlValues: {},
  };

  export const useCreateOwnerState = (): CreateOwnerState => {
    const { isDarkMode } = useFsTheme();
    const { pushCreate } = useFsu();
    const { openAsset } = useFsNav();
    const { selectOptions } = useFsDirent();

    const [isExpanded, setIsExpanded] = React.useState(false);
    const [fields, setFields] = React.useState<TextFields>({
      serviceName: _initProps.serviceName,
      intlValues: _initProps.intlValues,
    });
    const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

    const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

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

    async function onSave() {
      try {
        const dirent = await pushCreate(state);
        openAsset(dirent);
      } catch (err) {
        console.error('PRINTOUT create failed', err);
      }
    }

    function onCancel() {
      setFields({
        serviceName: _initProps.serviceName,
        intlValues: _initProps.intlValues,
      });
      setStateRaw(new _CreateState(_initProps));
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
      onSave,
      onCancel,
    };
  };
