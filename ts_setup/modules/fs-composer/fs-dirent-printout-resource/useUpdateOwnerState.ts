import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentSelectMultiOption } from '../fs-utilities';

type _ChangeStateProps = {
  resourceId: string;
  bodyType: Fs.BodyType;
  resourceName: string;
  labels: string[];
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
  treeId: string;
}

export interface UpdateOwnerState {
  assetPath: string | undefined;
  isDirty: boolean;
  resourceName: string;
  labels: string[];
  labelOptions: string[];
  contentType: string;
  uploadBody: string;
  printoutPageIds: string[];
  printoutPageOptions: FsDirentSelectMultiOption[];
  onChangeResourceName: (value: string) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeUploadBody: (value: string) => void;
  onChangePrintoutPageIds: (value: string[]) => void;
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
  get treeId() { return this._current.treeId; }
  get bodyType() {
    return this._current.bodyType;
  }
  get resourceName() {
    return this._current.resourceName;
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
  get isDirty(): boolean {
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
        labels: c.labels.length ? c.labels : undefined,
        uploadBody: c.uploadBody || undefined,
        printoutPageIds: c.printoutPageIds,
      },
    };
  }

  withResourceName(resourceName: string): _ChangeState {
    return new _ChangeState({ ...this._current, resourceName }, this._origin);
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


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent, selectOptions, getDirentName } = useFsDirent();

  const dirent = getDirent(props.direntId)!;
  const resourceProps = dirent.props as Fs.PrintoutResourceProps;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    resourceId: props.direntId,
    bodyType: dirent.type,
    treeId: dirent?.commitIndex?.treeId!,
    resourceName: resourceProps.resourceName ?? dirent.name ?? '',
    labels: (resourceProps.labels ?? []).map(l => l.key),
    contentType: resourceProps.contentType ?? '',
    uploadBody: '',
    printoutPageIds: resourceProps.printoutPageIds ?? [],
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

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
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }
  function onChangeUploadBody(value: string) {
    setState(prev => prev.withUploadBody(value));
  }
  function onChangePrintoutPageIds(value: string[]) {
    setState(prev => prev.withPrintoutPageIds(value));
  }
  return {
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    resourceName: state.resourceName,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    contentType: state.contentType,
    uploadBody: state.uploadBody,
    printoutPageIds: state.printoutPageIds,
    printoutPageOptions,
    onChangeResourceName,
    onChangeLabels,
    onChangeUploadBody,
    onChangePrintoutPageIds,
  };
};
