import React from 'react';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';

export interface CreateOwnerState {
  isDirty: boolean;
  serviceId: string;
  localeId: string;
  printoutOptions: Fs.SelectOption[];
  localeOptions: Fs.SelectOption[];
  onChangeServiceId: (value: string) => void;
  onChangeLocaleId: (value: string) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  serviceId: string;
  localeId: string;
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

  get isDirty(): boolean {
    return !!this._current.serviceId && !!this._current.localeId;
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        serviceId: this._current.serviceId,
        localeId: this._current.localeId,
      }
    };
  }

  withServiceId(serviceId: string): _CreateState {
    return new _CreateState({ ...this._current, serviceId, localeId: '' }, this._origin);
  }
  withLocaleId(localeId: string): _CreateState {
    return new _CreateState({ ...this._current, localeId }, this._origin);
  }

}

const _init: _CreateStateProps = {
  bodyType: 'PRINTOUT_PAGE',
  serviceId: '',
  localeId: '',
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { selectOptions, createDirent } = useFsDirent();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

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
  async function onSave() {
    await createDirent(state);
  }

  return {
    isDirty: state.isDirty,
    serviceId: state.serviceId,
    localeId: state.localeId,
    printoutOptions: selectOptions.printouts,
    localeOptions,
    onChangeServiceId,
    onChangeLocaleId,
    onSave,
  };
};
