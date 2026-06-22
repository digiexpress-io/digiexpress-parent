import React from 'react';
import { Fs, FsuCreateChange } from '@dxs-ts/fs-api';


type _CreateStateProps = {
  bodyType: Fs.BodyType;
  name: string;
  parentId: string | undefined;
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
    return {
      bodyType: this._current.bodyType,
      changes: {
        name: this._current.name || undefined,
        parentId: this._current.parentId,
      },
    };
  }

  withName(name: string): _CreateState {
    return new _CreateState({ ...this._current, name }, this._origin);
  }
}

function getLocationPath(parentFolder: Fs.DirentBase | undefined): string {
  if (!parentFolder) {
    return '';
  }
  if (parentFolder.type !== 'FOLDER') {
    return parentFolder.fullPath.split('/').slice(0, -1).join('/');
  }
  return parentFolder.fullPath;
}

export interface CreateOwnerState {
  isDirty: boolean;
  locationPath: string;
  name: string;
  onChangeName: (value: string) => void;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined }): CreateOwnerState => {

  const parentId = props.parentFolder?.type === 'FOLDER' ? props.parentFolder.id : undefined;

  const _init: _CreateStateProps = {
    bodyType: 'FOLDER',
    name: '',
    parentId,
  };

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  const locationPath = getLocationPath(props.parentFolder);

  function onChangeName(value: string) {
    setState(prev => prev.withName(value));
  }

  return {
    isDirty: state.isDirty,
    locationPath,
    name: state.name,
    onChangeName,
  };
};
