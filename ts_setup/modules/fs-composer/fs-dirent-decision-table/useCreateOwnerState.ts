import React from 'react';

import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  name: string;
  isDirty: boolean;
  onChangeName: (v: string) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
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
  get name() {
    return this._current.name;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const current = this._current;
    return {
      bodyType: current.bodyType,
      changes: {
        name: current.name || undefined,
        nodes: [],
      },
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
}

const _init: _CreateStateProps = { bodyType: 'DECISION_TABLE', name: '' };

export const useCreateOwnerState = (): CreateOwnerState => {
  const { createDirent } = useFsDirent();
  const { openAsset } = useFsNav();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  function onChangeName(v: string) {
    setState(prev => prev.withName(v));
  }
  async function onSave() {
    const newDirent = await createDirent(state);
    openAsset(newDirent);
  }

  return {
    name: state.name,
    isDirty: state.isDirty,
    onChangeName,
    onSave,
  };
};
