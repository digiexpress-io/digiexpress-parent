import React from 'react';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  serviceId: string;
  localeId: string;
  content: string;
  printoutOptions: Fs.SelectOption[];
  localeOptions: Fs.SelectOption[];
  onChangeServiceId: (value: string) => void;
  onChangeLocaleId: (value: string) => void;
  onChangeContent: (value: string) => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  serviceId: string;
  localeId: string;
  content: string;
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
}

const _init: _CreateStateProps = {
  bodyType: 'PRINTOUT_PAGE',
  serviceId: '',
  localeId: '',
  content: ''
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions } = useFsDirent();

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
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }

  return {
    isDarkMode,
    isChanged: state.isChanged,
    serviceId: state.serviceId,
    localeId: state.localeId,
    content: state.content,
    printoutOptions: selectOptions.printouts,
    localeOptions,
    onChangeServiceId,
    onChangeLocaleId,
    onChangeContent
  };
};
