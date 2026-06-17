import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsDirent, FsuCreateChange } from '@dxs-ts/fs-api';
import { FsDirentSelectMultiOption } from '../fs-utilities';

export interface CreateOwnerState {
  isDarkMode: boolean;
  isDirty: boolean;
  resourceName: string;
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
  contentTypeOptions: Fs.SelectOption[];
  printoutPageOptions: FsDirentSelectMultiOption[];
  onChangeResourceName: (value: string) => void;
  onChangeContentType: (value: string) => void;
  onChangeUploadBody: (value: string) => void;
  onChangePrintoutPageIds: (value: string[]) => void;
  onSave: () => Promise<void>;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  resourceName: string;
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
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
  get printoutPageIds() {
    return this._current.printoutPageIds;
  }
  get isDirty(): boolean {
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
        printoutPageIds: c.printoutPageIds,
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
  withPrintoutPageIds(printoutPageIds: string[]): _CreateState {
    return new _CreateState({ ...this._current, printoutPageIds }, this._origin);
  }
}

const _contentTypeOptions: Fs.SelectOption[] = [
  { value: 'image/*', label: 'Image' },
  { value: 'text/*', label: 'Text' },
];

const _init: _CreateStateProps = {
  bodyType: 'PRINTOUT_RESOURCE',
  resourceName: '',  
  contentType: 'image/*',
  uploadBody: '',
  printoutPageIds: [],
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions, getDirentName, createDirent } = useFsDirent();

  const [state, setState] = React.useState<_CreateState>(() => new _CreateState(_init));

  const printoutPageOptions: FsDirentSelectMultiOption[] = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_PAGE')
    .map(p => {
      const pageProps = p as Fs.PrintoutPageProps;
      const printoutLabel = selectOptions.printouts.find(opt => opt.value === pageProps.serviceId)?.label ?? pageProps.serviceId;
      const localeName = getDirentName(pageProps.id) ?? pageProps.id;
      return { value: pageProps.id, label: `${printoutLabel} / ${localeName}` };
    });

  function onChangeResourceName(value: string) {
    setState(prev => prev.withResourceName(value));
  }
  function onChangeContentType(value: string) {
    setState(prev => prev.withContentType(value));
  }
  function onChangeUploadBody(value: string) {
    setState(prev => prev.withUploadBody(value));
  }
  function onChangePrintoutPageIds(value: string[]) {
    setState(prev => prev.withPrintoutPageIds(value));
  }
  async function onSave() {
    await createDirent(state);
  }

  return {
    isDarkMode,
    isDirty: state.isDirty,
    resourceName: state.resourceName,
    contentType: state.contentType,
    uploadBody: state.uploadBody,
    printoutPageIds: state.printoutPageIds,
    contentTypeOptions: _contentTypeOptions,
    printoutPageOptions,
    onChangeResourceName,
    onChangeContentType,
    onChangeUploadBody,
    onChangePrintoutPageIds,
    onSave,
  };
};
