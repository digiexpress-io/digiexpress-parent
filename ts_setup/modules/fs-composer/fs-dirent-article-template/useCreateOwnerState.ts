import React from 'react';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';


type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
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
  get name() {
    return this._current.name;
  }
  get content() {
    return this._current.content;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      changes: {
        name: this._current.name || undefined,
        type: this._current.bodyType,
        content: this._current.content || undefined,
      },
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
  withContent(content: string): _CreateState {
    return new _CreateState({ ...this._current, content }, this._origin);
  }
}

const _init: _CreateStateProps = {
  bodyType: 'ARTICLE_TEMPLATE',
  name: '',
  content: '',
};

export interface CreateOwnerState {
  isDirty: boolean;
  name: string;
  content: string;
  onChangeName: (value: string) => void;
  onChangeContent: (value: string) => void;
  onSave: () => Promise<void>;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { createDirent } = useFsDirent();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }
  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }
  async function onSave() {
    await createDirent(state);
  }

  return ({
    isDirty: state.isDirty,
    name: state.name,
    content: state.content,
    onChangeName,
    onChangeContent,
    onSave,
  });
};
