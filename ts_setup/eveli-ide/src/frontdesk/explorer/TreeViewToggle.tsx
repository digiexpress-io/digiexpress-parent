const findMainId = (values: string[]) => {
  const result = values.filter(id => !id.endsWith("-nested"));
  if (result.length) {
    return result[0];
  }
  return undefined;
}

class TreeViewToggle {
  private _expanded: string[];
  private _main?: string;

  constructor(expanded?: string[], main?: string) {
    this._expanded = expanded ? expanded : [];
    this._main = main;
  }
  get expanded() { return this._expanded; }

  onNodeToggle(nodeIds: string[]): TreeViewToggle {
    const newId = findMainId(nodeIds.filter(n => n !== this._main));

    // remove main id when switching
    if (this._main !== newId && this._main && newId) {
      nodeIds.splice(nodeIds.indexOf(this._main), 1);
    }

    // expand options
    const mainAndNew: boolean = (this._main && newId) ? true : false;
    if (this._main !== newId && (mainAndNew || this._expanded.length === 0)) {
      const options = newId + 'options-nested';
      if (!nodeIds.includes(options)) {
        nodeIds.push(options);
      }
    }

    return new TreeViewToggle(nodeIds, newId);
  }
}


export { TreeViewToggle };