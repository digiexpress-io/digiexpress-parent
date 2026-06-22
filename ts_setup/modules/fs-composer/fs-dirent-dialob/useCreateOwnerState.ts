import React from 'react';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';



export interface CreateOwnerState {
  isDirty: boolean;
  formName: string;
  formTechnicalId: string;
  onChangeFormName: (value: string) => void;
  onChangeFormTechnicalId: (value: string) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  formName: string;
  formTechnicalId: string;
}

class _CreateState implements FsuCreateChange {
  private _origin: _CreateStateProps;
  private _current: _CreateStateProps;

  constructor(props: _CreateStateProps, origin?: _CreateStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get bodyType() { return this._current.bodyType; }
  get formName() { return this._current.formName; }
  get formTechnicalId() { return this._current.formTechnicalId; }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        formName: c.formName || undefined,
        formTechnicalId: c.formTechnicalId || undefined,
      },
    };
  }

  withFormName(formName: string): _CreateState {
    return new _CreateState({ ...this._current, formName }, this._origin);
  }
  withFormTechnicalId(formTechnicalId: string): _CreateState {
    return new _CreateState({ ...this._current, formTechnicalId }, this._origin);
  }
}

const _initProps: _CreateStateProps = {
  bodyType: 'DIALOB_FORM',
  formName: '',
  formTechnicalId: '',
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { createDirent } = useFsDirent();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_initProps));

  function onChangeFormName(value: string) {
    setState(prev => prev.withFormName(value));
  }
  function onChangeFormTechnicalId(value: string) {
    setState(prev => prev.withFormTechnicalId(value));
  }
  async function onSave() {
    await createDirent(state);
  }

  return {
    isDirty: state.isDirty,
    formName: state.formName,
    formTechnicalId: state.formTechnicalId,
    onChangeFormName,
    onChangeFormTechnicalId,
    onSave,
  };
};
