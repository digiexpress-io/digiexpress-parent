import { Fs } from "../fs-types";



export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}
export class FsWorld {

  private _hi_dirents: Fs.DirentBase[];
  private _flat_dirents: Record<string, Fs.DirentBase>;
  private _selectOptions:  Fs.SelectOptions | undefined;

  constructor(props: {
    dirents: Fs.DirentBase[];
  }) {
    this._hi_dirents = props.dirents;
    this._flat_dirents = _flattenDirents(props.dirents);
  }
  public getDirent(id: string): Fs.DirentBase | undefined {
    const dirent = this._flat_dirents[id];

    if (!dirent) {
      return undefined;
    }
    return dirent;
  }

  public get dirents(): Fs.DirentBase[] {
    return [...this._hi_dirents];
  }
  public findAllDirents(): Fs.DirentBase[] {
    return Object.values(this._flat_dirents);
  }

  public get selectOptions(): Fs.SelectOptions {
    if(this._selectOptions == undefined) {
      const dirents = Object.values(this._flat_dirents)
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
        labels: _collectLabels(Object.values(propsMap)),
        direntProps: propsMap,

        collectDialobTags: (dialobId: string): Fs.SelectOption[] => {
          const entry = propsMap[dialobId];
          if (!entry || entry.type !== 'DIALOB_FORM') { return []; }
    
          const tags = (entry as Fs.DialobProps).versionTags;
    
          if (!tags || tags.length === 0) { return []; }
          return tags.map(tag => ({ value: tag, label: tag }));
        },
        getActiveDialobTag: (props: Fs.DialobProps): string => {
          const tags = props.versionTags;
          if (!tags || tags.length === 0) { return 'LATEST'; }
          return tags[tags.length - 1];
        }
      }
    }

    return this._selectOptions;
  }

  public isChildError(dirent: Fs.DirentBase): boolean {
    const direntProps = dirent.props;
    if (direntProps?.errors && direntProps.errors.length > 0) {
      return true;
    }
    if (dirent.children) {
      return dirent.children.some(child => this.isChildError(child));
    }
    return false;
  };


  public findReferencesToDirent(targetDirent: Fs.DirentBase): ItemReferencesEntry[] {
    const references: ItemReferencesEntry[] = [];
    for(const dirent of Object.values(this._flat_dirents)) {
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
  nodes.forEach(node => _collectDirents(result, node));
  return result;
}
function _collectDirents(result: Record<string, Fs.DirentBase>, node: Fs.DirentBase): void {
  result[node.id] = node;
  node.children.forEach(child => _collectDirents(result, child));
}

function _collectArticles(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'ARTICLE') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(..._collectArticles(node.children)); }
  });
  return result;
}

function _collectFlows(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'FLOW') { result.push({ value: node.name, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(..._collectFlows(node.children)); }
  });
  return result;
}

function _collectDialobs(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'DIALOB_FORM') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(..._collectDialobs(node.children)); }
  });
  return result;
}

function _collectLanguages(nodes: Fs.DirentBase[]): string[] {
  const result: string[] = [];
  nodes.forEach(node => {
    if (node.type === 'LOCALE') { result.push(node.name.replace('.language', '')); }
    if (node.children && node.children.length > 0) { result.push(..._collectLanguages(node.children)); }
  });
  return result;
}

function _collectLabels(propsMap: Fs.PropsBase[]): string[] {
  const labelSet = new Set<string>();
  propsMap.forEach(entry => {
    entry.labels.forEach(l => labelSet.add(l.value));
  });
  return Array.from(labelSet).sort();
}