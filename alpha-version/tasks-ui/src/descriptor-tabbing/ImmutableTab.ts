import { Tab, TabId, TabBody, TabSelection, SelectionOptions } from './tabbing-types';

export interface Init<I extends TabId, B extends TabBody> {
  id: I;
  body: B; 
  selected?: readonly TabSelection[];
  active?: boolean;
}

export class ImmutableTab<I extends TabId, B extends TabBody> implements Tab<I, B> {
  private _id: I;
  private _body: B;
  private _selected: readonly TabSelection[];
  private _active: boolean;

  constructor(init: Init<I, B>) {
    this._id = init.id;
    this._body = init.body;
    this._selected = Object.freeze(init.selected ?? []);
    this._active = init.active ?? false;
  }
  get id() { return this._id; }
  get selected() { return this._selected; }
  get active() { return this._active; }
  get body() { return this._body; }

  withBody(body: B) {
    return new ImmutableTab<I, B>(this.clone({ body }));
  }

  withSelected(newSelection: TabSelection[], options?: SelectionOptions) {
    const selected: TabSelection[] = [];
    if(options?.disableOthers === true || options?.disableOthers === undefined) {
      selected.push(...newSelection);
    } else {
      selected.push(...this.selected);
      for(const item of newSelection) {
        if(selected.includes(item)) {
          continue;
        }
        selected.push(item);
      }
    }

    return new ImmutableTab<I, B>(this.clone({ selected }));
  }
  withActive(active: boolean) {
    return new ImmutableTab<I, B>(this.clone({ active }));
  }
  
  clone(add: Partial<Init<I, B>>): Init<I, B> {
    return {
      body: this._body,
      id: this._id,
      selected: this.selected,
      active: this._active,

      ...add
    };
  }
}