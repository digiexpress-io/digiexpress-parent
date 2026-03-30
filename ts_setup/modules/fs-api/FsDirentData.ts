import { DialobDirentProps, FsDirent, FsDirentProps, FsDirentType } from "./fs-types";

interface SelectOption {
  value: string;
  label: string;
}

const ALL_CONFIG_OPTIONS: SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];


export class FsDirentData {
  private _articles: SelectOption[];
  private _flows: SelectOption[];
  private _dialobs: SelectOption[];
  private _languages: string[];
  private _direntProps: Record<string, FsDirentProps>;

  constructor(dirents: FsDirent[], direntProps: Record<string, FsDirentProps>) {
    this._articles = this.collectArticles(dirents);
    this._flows = this.collectFlows(dirents);
    this._dialobs = this.collectDialobs(dirents);
    this._languages = this.collectLocales(dirents);
    this._direntProps = direntProps;
  }

  get articles() { return this._articles };
  get flows() { return this._flows };
  get dialobs() { return this._dialobs };
  get languages() { return this._languages };
  get direntProps() { return this._direntProps }

  public collectDialobs(nodes: FsDirent[]): SelectOption[] {
    const result: { value: string; label: string }[] = [];
    nodes.forEach(node => {
      if (node.type === 'dialob') {
        result.push({ value: node.id, label: node.name });
      }
      if (node.children && node.children.length > 0) {
        result.push(...this.collectDialobs(node.children));
      }
    });
    return result;
  }

  public getActiveDialobTag(props: DialobDirentProps): string {
    const tags = props.versionTags;
    if (!tags || tags.length === 0) {
      return 'LATEST';
    }
    return tags[tags.length - 1];
  }

  public collectDialobTags(dialobId: string): SelectOption[] {
    const entry = this._direntProps[dialobId];
    if (!entry || entry.type !== 'dialob') {
      return [];
    }
    const tags = (entry as DialobDirentProps).versionTags;
    if (!tags || tags.length === 0) {
      return [];
    }
    return tags.map(tag => ({ value: tag, label: tag }));
  }

  private collectFlows(nodes: FsDirent[]): SelectOption[] {
    const result: { value: string; label: string }[] = [];
    nodes.forEach(node => {
      if (node.type === 'flow') {
        result.push({ value: node.name, label: node.name });
      }
      if (node.children && node.children.length > 0) {
        result.push(...this.collectFlows(node.children));
      }
    });
    return result;
  }

  public collectLocales(nodes: FsDirent[]): string[] {
    const result: string[] = [];
    nodes.forEach(node => {
      if (node.type === 'language') {
        result.push(node.name.replace('.language', ''));
      }
      if (node.children && node.children.length > 0) {
        result.push(...this.collectLocales(node.children));
      }
    });
    return result;
  }

  public collectArticles(nodes: FsDirent[]): SelectOption[] {
    const result: { value: string; label: string }[] = [];
    nodes.forEach(node => {
      if (node.type === 'article') {
        result.push({ value: node.id, label: node.name });
      }
      if (node.children && node.children.length > 0) {
        result.push(...this.collectArticles(node.children));
      }
    });
    return result;
  }

  static getConfigOptionsForType(type: FsDirentType): SelectOption[] {
    switch (type) {
      case 'link':
      case 'phone': {
        return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode' || o.value === 'disabledMode');
      }
      case 'service':
      case 'article': {
        return ALL_CONFIG_OPTIONS;
      }
      case 'language': {
        return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode');
      }
      case 'page': {
        return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode' || o.value === 'devMode');
      }
      case 'printout': {
        return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode');
      }
      default: {
        return [];
      }
    }
  }


}

