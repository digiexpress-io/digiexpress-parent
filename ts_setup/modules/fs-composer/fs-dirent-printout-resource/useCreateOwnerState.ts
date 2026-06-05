import React from 'react';
import { useFsTheme } from '../fs-theme';
import { Fs, useFsDirent, useFsu, FsuCreateChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentSelectMultiOption } from '../fs-dirent-select-multi';

interface _TextFields {
  resourceName: string;
  assetDescription: { text: string };
}

export interface CreateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  resourceName: string;
  assetDescription: { text: string };
  labels: string[];
  labelOptions: string[];
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
  contentTypeOptions: Fs.SelectOption[];
  printoutPageOptions: FsDirentSelectMultiOption[];
  onChangeResourceName: (v: string) => void;
  onBlurResourceName: () => void;
  onChangeDescription: (v: string) => void;
  onBlurDescription: () => void;
  onChangeLabels: (value: string[]) => void;
  onChangeContentType: (v: string) => void;
  onChangeUploadBody: (body: string) => void;
  onChangePrintoutPageIds: (value: string[]) => void;
  onSave: () => void;
  onCancel: () => void;
}

type _CreateStateProps = {
  bodyType: Fs.BodyType;
  resourceName: string;
  assetDescription: { text: string };
  labels: string[];
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
  get assetDescription() {
    return this._current.assetDescription;
  }
  get labels() {
    return this._current.labels;
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
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      changes: {
        resourceName: c.resourceName || undefined,
        assetDescription: c.assetDescription || undefined,
        labels: c.labels.length ? c.labels : undefined,
        contentType: c.contentType || undefined,
        uploadBody: c.uploadBody || undefined,
        printoutPageIds: c.printoutPageIds,
      },
    };
  }

  withResourceName(resourceName: string): _CreateState {
    return new _CreateState({ ...this._current, resourceName }, this._origin);
  }
  withDescription(assetDescription: { text: string }): _CreateState {
    return new _CreateState({ ...this._current, assetDescription }, this._origin);
  }
  withLabels(labels: string[]): _CreateState {
    return new _CreateState({ ...this._current, labels }, this._origin);
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

const _initProps: _CreateStateProps = {
  bodyType: 'PRINTOUT_RESOURCE',
  resourceName: '',
  assetDescription: { text: '' },
  labels: [],
  contentType: 'image/*',
  uploadBody: '',
  printoutPageIds: [],
};

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions, getDirentName } = useFsDirent();
  const { pushCreate } = useFsu();
  const { openAsset } = useFsNav();

  const [fields, setFields] = React.useState<_TextFields>({
    resourceName: _initProps.resourceName,
    assetDescription: _initProps.assetDescription,
  });
  const [state, setStateRaw] = React.useState<_CreateState>(() => new _CreateState(_initProps));

  const setState = (cb: (prev: _CreateState) => _CreateState) => setStateRaw(cb);

  const printoutPageOptions: FsDirentSelectMultiOption[] = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_PAGE')
    .map(p => {
      const pageProps = p as Fs.PrintoutPageProps;
      const printoutLabel = selectOptions.printouts.find(opt => opt.value === pageProps.serviceId)?.label ?? pageProps.serviceId;
      const localeName = getDirentName(pageProps.id) ?? pageProps.id;
      return { value: pageProps.id, label: `${printoutLabel} / ${localeName}` };
    });

  const isChangesPresent = state.isChanged
    || fields.resourceName !== state.resourceName
    || fields.assetDescription.text !== state.assetDescription.text;

  function onChangeResourceName(v: string) {
    setFields(prev => ({ ...prev, resourceName: v }));
  }
  function onBlurResourceName() {
    setState(prev => prev.withResourceName(fields.resourceName));
  }
  function onChangeDescription(v: string) {
    setFields(prev => ({ ...prev, assetDescription: { text: v } }));
  }
  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.assetDescription));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }
  function onChangeContentType(v: string) {
    setState(prev => prev.withContentType(v));
  }
  function onChangeUploadBody(body: string) {
    setState(prev => prev.withUploadBody(body));
  }
  function onChangePrintoutPageIds(value: string[]) {
    setState(prev => prev.withPrintoutPageIds(value));
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
    setFields({ resourceName: _initProps.resourceName, assetDescription: _initProps.assetDescription });
    setStateRaw(new _CreateState(_initProps));
  }

  return {
    isDarkMode,
    isChanged: isChangesPresent,
    resourceName: fields.resourceName,
    assetDescription: fields.assetDescription,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    contentType: state.contentType,
    uploadBody: state.uploadBody,
    printoutPageIds: state.printoutPageIds,
    contentTypeOptions: _contentTypeOptions,
    printoutPageOptions,
    onChangeResourceName,
    onBlurResourceName,
    onChangeDescription,
    onBlurDescription,
    onChangeLabels,
    onChangeContentType,
    onChangeUploadBody,
    onChangePrintoutPageIds,
    onSave,
    onCancel,
  };
};
