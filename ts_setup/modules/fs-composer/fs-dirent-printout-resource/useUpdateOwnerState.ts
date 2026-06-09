import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectMultiOption } from '../fs-dirent-select-multi';

type _ChangeStateProps = {
  resourceId: string;
  bodyType: Fs.BodyType;
  resourceName: string;
  assetDescription: { text: string };
  labels: string[];
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
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

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.resourceId,
      changes: {
        resourceId: c.resourceId,
        resourceName: c.resourceName || undefined,
        assetDescription: c.assetDescription || undefined,
        labels: c.labels.length ? c.labels : undefined,
        uploadBody: c.uploadBody || undefined,
        printoutPageIds: c.printoutPageIds,
      },
    };
  }

  withResourceName(resourceName: string): _ChangeState {
    return new _ChangeState({ ...this._current, resourceName }, this._origin);
  }
  withDescription(assetDescription: { text: string }): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._origin);
  }
  withLabels(labels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
  withUploadBody(uploadBody: string): _ChangeState {
    return new _ChangeState({ ...this._current, uploadBody }, this._origin);
  }
  withPrintoutPageIds(printoutPageIds: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, printoutPageIds }, this._origin);
  }
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  resourceName: string;
  assetDescription: string;
  labels: string[];
  labelOptions: string[];
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
  printoutPageOptions: FsDirentSelectMultiOption[];
  onChangeResourceName: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeUploadBody: (value: string) => void;
  onChangePrintoutPageIds: (value: string[]) => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions, getDirentName } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const resourceProps = dirent.props as Fs.PrintoutResourceProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    resourceId: props.direntId,
    bodyType: dirent.type,
    resourceName: resourceProps.resourceName ?? dirent.name ?? '',
    assetDescription: { text: resourceProps.assetDescription ?? '' },
    labels: (resourceProps.labels ?? []).map(l => l.value),
    contentType: resourceProps.contentType ?? '',
    uploadBody: '',
    printoutPageIds: resourceProps.printoutPageIds ?? [],
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

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
  function onChangeDescription(value: string) {
    setState(prev => prev.withDescription({ text: value }));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }
  function onChangeUploadBody(value: string) {
    setState(prev => prev.withUploadBody(value));
  }
  function onChangePrintoutPageIds(value: string[]) {
    setState(prev => prev.withPrintoutPageIds(value));
  }
  function onCancel() {
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    isChanged: state.isChanged,
    resourceName: state.resourceName,
    assetDescription: state.assetDescription.text,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    contentType: state.contentType,
    uploadBody: state.uploadBody,
    printoutPageIds: state.printoutPageIds,
    printoutPageOptions,
    onChangeResourceName,
    onChangeDescription,
    onChangeLabels,
    onChangeUploadBody,
    onChangePrintoutPageIds,
    onCancel,
  };
};
