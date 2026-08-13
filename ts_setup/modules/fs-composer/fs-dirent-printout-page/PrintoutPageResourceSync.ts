import { Fs } from '@dxs-ts/fs-api';

const RESOURCE_REF_PATTERN = /sys\.inputs\.resources\.at\("([^"]+)"\)/g;

export interface ResourceSyncChange {
  toLink: Fs.PrintoutResourceProps[];
  toUnlink: Fs.PrintoutResourceProps[];
}

export class PrintoutPageResourceSync {
  private readonly _pageId: string;
  private readonly _resources: Fs.PrintoutResourceProps[];

  constructor(pageId: string, resources: Fs.PrintoutResourceProps[]) {
    this._pageId = pageId;
    this._resources = resources;
  }

  extractReferencedNames(content: string): Set<string> {
    const names = new Set<string>();
    const pattern = new RegExp(RESOURCE_REF_PATTERN.source, 'g');
    let match = pattern.exec(content);
    while (match !== null) {
      names.add(match[1]);
      match = pattern.exec(content);
    }
    return names;
  }

  computeChanges(content: string): ResourceSyncChange {
    const referenced = this.extractReferencedNames(content);
    const toLink: Fs.PrintoutResourceProps[] = [];
    const toUnlink: Fs.PrintoutResourceProps[] = [];

    for (const resource of this._resources) {
      const isLinked = resource.printoutPageIds.includes(this._pageId);
      const isReferenced = referenced.has(resource.resourceName);

      if (isLinked && !isReferenced) {
        toUnlink.push(resource);
      } else if (!isLinked && isReferenced) {
        toLink.push(resource);
      }
    }

    return { toLink, toUnlink };
  }
}
