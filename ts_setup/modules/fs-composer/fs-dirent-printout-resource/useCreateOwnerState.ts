import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';

interface _TextFields {
  resourceName: string;
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  resourceName: string;
  contentType: string;
  uploadBody: string;
  contentTypeOptions: Fs.SelectOption[];
  onChangeResourceName: (v: string) => void;
  onBlurResourceName: () => void;
  onChangeContentType: (v: string) => void;
  onChangeUploadBody: (body: string) => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  resourceName: string;
  contentType: string;
  uploadBody: string;
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

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        resourceName: c.resourceName || undefined,
        contentType: c.contentType || undefined,
        uploadBody: c.uploadBody || undefined,
        printoutPageIds: [],
      },
    };
  }

  withResourceName(resourceName: string): _CreateState {
    return new _CreateState({ ...this._current, resourceName }, this._origin);
  }
  withContentType(contentType: string): _CreateState {
    return new _CreateState({ ...this._current, contentType, uploadBody: '' }, this._origin);
  }
  withUploadBody(uploadBody: string): _CreateState {
    return new _CreateState({ ...this._current, uploadBody }, this._origin);
  }
}

const _contentTypeOptions: Fs.SelectOption[] = [
  { value: 'image/*', label: 'Image' },
  { value: 'text/*', label: 'Text' },
];

const _initProps: _CreateStateProps = {
  bodyType: 'PRINTOUT_RESOURCE',
  resourceName: '',
  contentType: 'image/*',
  uploadBody: '',
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [fields, setFields] = React.useState<_TextFields>({
    resourceName: _initProps.resourceName,
  });
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const isChangesPresent = state.isChanged
    || fields.resourceName !== state.resourceName;

  function onChangeResourceName(v: string) {
    setFields(prev => ({ ...prev, resourceName: v }));
  }
  function onBlurResourceName() {
    setState(prev => prev.withResourceName(fields.resourceName));
  }
  function onChangeContentType(v: string) {
    setState(prev => prev.withContentType(v));
  }
  function onChangeUploadBody(body: string) {
    setState(prev => prev.withUploadBody(body));
  }

  async function onSave() {
    try {
      const dirent = await pushCreate(state);
      openAsset(dirent);
    } catch {
      // error snackbar already shown by pushCreate
    }
  }

  function onCancel() {
    setFields({ resourceName: _initProps.resourceName });
    setStateRaw(new _CreateState(_initProps));
  }

  return {
    isDarkMode,
    isChanged: isChangesPresent,
    resourceName: fields.resourceName,
    contentType: state.contentType,
    uploadBody: state.uploadBody,
    contentTypeOptions: _contentTypeOptions,
    onChangeResourceName,
    onBlurResourceName,
    onChangeContentType,
    onChangeUploadBody,
    onSave,
    onCancel,
  };
};
