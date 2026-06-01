import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';

type _ChangeStateProps = {
  resourceId: string;
  bodyType: Fs.BodyType;
  resourceName: string;
  contentType: string;
  uploadBody: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.resourceId;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get resourceName() {
    return this._current.resourceName;
  }
  get contentType() {
    return this._current.contentType;
  }
  get uploadBody() {
    return this._current.uploadBody;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.resourceId,
      changes: {
        resourceId: c.resourceId,
        resourceName: c.resourceName || undefined,
        uploadBody: c.uploadBody || undefined,
      },
    };
  }

  withResourceName(resourceName: string): _ChangeState {
    return new _ChangeState({ ...this._current, resourceName }, this._origin);
  }
  withUploadBody(uploadBody: string): _ChangeState {
    return new _ChangeState({ ...this._current, uploadBody }, this._origin);
  }
}

interface _TextFields {
  resourceName: string;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  resourceName: string;
  contentType: string;
  uploadBody: string;
  onChangeResourceName: (v: string) => void;
  onBlurResourceName: () => void;
  onChangeUploadBody: (body: string) => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const resourceProps = dirent.props as Fs.PrintoutResourceProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    resourceId: props.direntId,
    bodyType: dirent.type,
    resourceName: resourceProps.resourceName ?? dirent.name ?? '',
    contentType: resourceProps.contentType ?? '',
    uploadBody: '',
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [fields, setFields] = React.useState<_TextFields>({
    resourceName: resourceProps.resourceName ?? dirent.name ?? '',
  });

  const isChangesPresent = state.isChanged
    || fields.resourceName !== state.resourceName;

  function onChangeResourceName(v: string) {
    setFields(prev => ({ ...prev, resourceName: v }));
  }
  function onBlurResourceName() {
    setState(prev => prev.withResourceName(fields.resourceName));
  }
  function onChangeUploadBody(body: string) {
    setState(prev => prev.withUploadBody(body));
  }
  function onCancel() {
    setFields({ resourceName: resourceProps.resourceName ?? dirent.name ?? '' });
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    isChanged: isChangesPresent,
    resourceName: fields.resourceName,
    contentType: state.contentType,
    uploadBody: state.uploadBody,
    onChangeResourceName,
    onBlurResourceName,
    onChangeUploadBody,
    onCancel,
  };
};
