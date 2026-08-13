import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { PrintoutPageSync } from './PrintoutPageResourceSync';

export interface UpdateOwnerState {
  assetPath: string | undefined;
  isDirty: boolean;
  content: string;
  connectedResourceNames: string[];
  onChangeContent: (value: string) => void;
  push: () => Promise<void>;
  syncLinks: (content: string) => Promise<void>;
}

type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  content: string;
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.pageId;
  }
  get treeId() { return this._current.treeId; }
  get bodyType() {
    return this._current.bodyType;
  }
  get content() {
    return this._current.content;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.pageId,
      changes: {
        pageId: c.pageId,
        content: c.content || undefined,
      },
    };
  }

  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { selectOptions, getDirent, updateDirent } = useFsDirent();

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PrintoutPageProps;

  const { state, update, push } = useFsuChange(props.direntId, () => new _ChangeState({
    pageId: props.direntId,
    bodyType: dirent.type,
    treeId: dirent?.commitIndex?.treeId!,
    content: pageProps.content ?? '',
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const connectedResourceNames = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_RESOURCE')
    .map(p => p as Fs.PrintoutResourceProps)
    .filter(p => p.printoutPageIds.includes(props.direntId))
    .map(p => p.resourceName);

  function onChangeContent(value: string) {
    setState(prev => prev.withContent(value));
  }

  async function syncLinks(content: string): Promise<void> {
    const allPrintoutResources = Object.values(selectOptions.direntProps)
      .filter((resource): resource is Fs.PrintoutResourceProps => resource.type === 'PRINTOUT_RESOURCE');

    const sync = new PrintoutPageSync(props.direntId, pageProps, allPrintoutResources, selectOptions.direntProps);
    const { resources, pageLinks } = sync.computeChanges(content);

    for (const resource of resources.toUnlink) {
      const resourceDirent = getDirent(resource.id);
      if (!resourceDirent) {
        continue;
      }
      await updateDirent({
        id: resource.id,
        treeId: resourceDirent.commitIndex?.treeId ?? '',
        bodyType: 'PRINTOUT_RESOURCE',
        isDirty: true,
        getCurrentProps: () => ({
          bodyType: 'PRINTOUT_RESOURCE',
          id: resource.id,
          changes: {
            resourceId: resource.id,
            printoutPageIds: resource.printoutPageIds.filter(pageId => pageId !== props.direntId),
          },
        }),
      });
    }

    for (const resource of resources.toLink) {
      const resourceDirent = getDirent(resource.id);
      if (!resourceDirent) {
        continue;
      }
      await updateDirent({
        id: resource.id,
        treeId: resourceDirent.commitIndex?.treeId ?? '',
        bodyType: 'PRINTOUT_RESOURCE',
        isDirty: true,
        getCurrentProps: () => ({
          bodyType: 'PRINTOUT_RESOURCE',
          id: resource.id,
          changes: {
            resourceId: resource.id,
            printoutPageIds: [...resource.printoutPageIds, props.direntId],
          },
        }),
      });
    }

    if (pageLinks.toLink.length > 0 || pageLinks.toUnlink.length > 0) {
      const newTemplateIds = new Set(pageProps.templateIds);
      for (const id of pageLinks.toUnlink) { newTemplateIds.delete(id); }
      for (const id of pageLinks.toLink) { newTemplateIds.add(id); }
      await updateDirent({
        id: props.direntId,
        treeId: dirent.commitIndex?.treeId ?? '',
        bodyType: 'PRINTOUT_PAGE',
        isDirty: true,
        getCurrentProps: () => ({
          bodyType: 'PRINTOUT_PAGE',
          id: props.direntId,
          changes: {
            pageId: props.direntId,
            templateIds: [...newTemplateIds],
          },
        }),
      });
    }
  }

  return {
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    content: state.content,
    connectedResourceNames,
    onChangeContent,
    push,
    syncLinks,
  };
};
