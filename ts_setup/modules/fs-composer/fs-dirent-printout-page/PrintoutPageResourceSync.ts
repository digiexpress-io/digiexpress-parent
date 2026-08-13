import { Fs } from '@dxs-ts/fs-api';

const RESOURCE_REF_PATTERN = /sys\.inputs\.resources\.at\("([^"]+)"\)/g;
const TEMPLATE_INCLUDE_PATTERN = /#include\s+"([^"]+)\.typ"/g;

export interface PrintoutPageSyncChanges {
  resources: {
    toLink: Fs.PrintoutResourceProps[];
    toUnlink: Fs.PrintoutResourceProps[];
  };
  pageLinks: {
    toLink: string[];
    toUnlink: string[];
  };
}

export class PrintoutPageSync {
  private readonly _pageId: string;
  private readonly _pageProps: Fs.PrintoutPageProps;
  private readonly _resources: Fs.PrintoutResourceProps[];
  private readonly _allProps: Record<string, Fs.Props>;

  constructor(
    pageId: string,
    pageProps: Fs.PrintoutPageProps,
    resources: Fs.PrintoutResourceProps[],
    allProps: Record<string, Fs.Props>
  ) {
    this._pageId = pageId;
    this._pageProps = pageProps;
    this._resources = resources;
    this._allProps = allProps;
  }

  computeChanges(content: string): PrintoutPageSyncChanges {
    return {
      resources: this._computeResourceChanges(content),
      pageLinks: this._computeIncludedResourceChanges(content),
    };
  }

  private _computeResourceChanges(content: string): PrintoutPageSyncChanges['resources'] {
    const referenced = new Set<string>();
    const pattern = new RegExp(RESOURCE_REF_PATTERN.source, 'g');
    let match = pattern.exec(content);
    while (match !== null) {
      referenced.add(match[1]);
      match = pattern.exec(content);
    }

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

  private _computeIncludedResourceChanges(content: string): PrintoutPageSyncChanges['pageLinks'] {
    const nameToPageId = new Map<string, string>();
    for (const [id, props] of Object.entries(this._allProps)) {
      if (props.type !== 'PRINTOUT_PAGE' || id === this._pageId) {
        continue;
      }
      const page = props as Fs.PrintoutPageProps;
      const serviceProps = this._allProps[page.serviceId] as Fs.PrintoutProps | undefined;
      const localeProps = this._allProps[page.localeId] as Fs.LanguageProps | undefined;
      if (!serviceProps) {
        continue;
      }
      const serviceName = serviceProps.printoutServiceName;
      const localeCode = localeProps?.localeCode;
      const templateName = localeCode ? `${serviceName} - ${localeCode}` : serviceName;
      nameToPageId.set(templateName, id);
    }

    const referencedPageIds = new Set<string>();
    const pattern = new RegExp(TEMPLATE_INCLUDE_PATTERN.source, 'g');
    let match = pattern.exec(content);
    while (match !== null) {
      const pageId = nameToPageId.get(match[1]);
      if (pageId !== undefined) {
        referencedPageIds.add(pageId);
      }
      match = pattern.exec(content);
    }

    const currentTemplateIds = new Set(this._pageProps.templateIds);
    const toLink = [...referencedPageIds].filter(id => !currentTemplateIds.has(id));
    const toUnlink = [...currentTemplateIds].filter(id => !referencedPageIds.has(id));
    return { toLink, toUnlink };
  }
}
