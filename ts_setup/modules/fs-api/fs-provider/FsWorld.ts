import { Fs } from "../fs-types";


export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}
export class FsWorld {

  private _hi_dirents: Fs.DirentBase[];
  private _flat_dirents: Record<string, Fs.DirentBase>;
  private _selectOptions: Fs.SelectOptions | undefined;
  private _article_parents: Record<string, string>;

  constructor(props: {
    dirents: Fs.DirentBase[];
  }) {
    this._flat_dirents = _flattenDirents(props.dirents);
    this._hi_dirents = props.dirents;
    this._article_parents = _buildArticleParents(props.dirents);
  }

  public getParentDirent(childId: string): Fs.DirentBase | undefined {
    const parentId = this._article_parents[childId];
    return parentId ? this._flat_dirents[parentId] : undefined;
  }

  public getDirentName(id: string): string | undefined {
    const dirent = this._flat_dirents[id];
    if (!dirent) {
      return undefined;
    }
    if (dirent.type === 'FOLDER') {
      return dirent.name;
    }
    if (dirent.type === 'ARTICLE') {
      return this._flat_dirents[this._article_parents[id]]?.name;
    }
    if (dirent.type === 'PRINTOUT_PAGE') {
      const pageProps = dirent.props as Fs.PrintoutPageProps | undefined;
      if (pageProps?.localeId) {
        return this._flat_dirents[pageProps.localeId]?.name ?? dirent.name;
      }
    }
    return dirent.name;
  }

  public getDirent(id: string): Fs.DirentBase | undefined {
    return this._flat_dirents[id];
  }

  public get dirents(): Fs.DirentBase[] {
    return this._hi_dirents;
  }

  public findAllDirents(): Fs.DirentBase[] {
    return Object.values(this._flat_dirents);
  }

  public get selectOptions(): Fs.SelectOptions {
    if (this._selectOptions == undefined) {
      const dirents = Object.values(this._flat_dirents);
      const propsMap = dirents
        .filter(e => !!e.props)
        .reduce((acc, e) => {
          const p = e.props!;
          acc[p.id] = p as Fs.Props;
          return acc;
        }, {} as Record<string, Fs.Props>);

      this._selectOptions = {
        articles: _collectArticles(dirents),
        flows: _collectFlows(dirents),
        dialobs: _collectDialobs(dirents),
        languages: _collectLanguages(dirents),
        printouts: _collectPrintouts(dirents),
        linkTypes: _collectLinkTypes(),
        labels: _collectLabels(dirents),
        direntProps: propsMap,

        collectDialobTags: (dialobId: string): Fs.SelectOption[] => {
          const entry = propsMap[dialobId];
          if (!entry || entry.type !== 'DIALOB_FORM') {
            return [];
          }
          const tags = (entry as Fs.DialobProps).versionTags;
          if (!tags || tags.length === 0) {
            return [];
          }
          return tags.map(tag => ({ value: tag, label: tag }));
        },
        getActiveDialobTag: (props: Fs.DialobProps): string => {
          const tags = props.versionTags;
          if (!tags || tags.length === 0) {
            return 'LATEST';
          }
          return tags[tags.length - 1];
        }
      };
    }

    return this._selectOptions;
  }

  public isChildError(dirent: Fs.DirentBase): boolean {
    if (dirent.props?.errors && dirent.props.errors.length > 0) {
      return true;
    }
    if (dirent.children) {
      return dirent.children.some(child => this.isChildError(child));
    }
    return false;
  };


  public findReferencesToDirent(targetDirent: Fs.DirentBase): ItemReferencesEntry[] {
    const references: ItemReferencesEntry[] = [];
    for (const dirent of Object.values(this._flat_dirents)) {
      if (dirent.props?.reference && dirent.name === targetDirent.name && dirent.id !== targetDirent.id) {
        references.push({
          assetName: dirent.name,
          location: dirent.fullPath
        });
      }
    }
    return references;
  };
}


function _flattenDirents(nodes: Fs.DirentBase[]): Record<string, Fs.DirentBase> {
  const result: Record<string, Fs.DirentBase> = {};
  function collect(items: Fs.DirentBase[]): void {
    items.forEach(node => {
      result[node.id] = node;
      if (node.children && node.children.length > 0) {
        collect(node.children);
      }
    });
  }
  collect(nodes);
  return result;
}

function _buildArticleParents(nodes: Fs.DirentBase[]): Record<string, string> {
  const result: Record<string, string> = {};
  function collect(items: Fs.DirentBase[], parentId?: string): void {
    items.forEach(node => {
      if (parentId) {
        result[node.id] = parentId;
      }
      if (node.children && node.children.length > 0) {
        collect(node.children, node.id);
      }
    });
  }
  collect(nodes);
  return result;
}

function _collectArticles(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'ARTICLE') {
      result.push({ value: node.id, label: node.name });
    }
  });
  return result;
}

function _collectFlows(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'FLOW') { result.push({ value: node.name, label: node.name }); }
  });
  return result;
}

function _collectDialobs(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'DIALOB_FORM' || node.type === 'DIALOB_FORM_META') {
      result.push({ value: node.id, label: node.name });
    }
  });
  return result;
}

function _collectLanguages(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'LOCALE') {
      result.push({ value: node.id, label: node.name })
    }
  });
  return Array.from(result);
}

function _collectPrintouts(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'PRINTOUT') {
      result.push({ value: node.id, label: node.name });
    }
  });
  return result;
}

function _collectLinkTypes(): Fs.LinkType[] {
  return ['internal', 'external', 'phone'];
}

function _collectLabels(nodes: Fs.DirentBase[]): string[] {
  const labelSet = new Set<string>();
  nodes.forEach(d => {
    (d.props?.labels ?? []).forEach(l => labelSet.add(l.value));
  });
  return Array.from(labelSet).sort();
}
