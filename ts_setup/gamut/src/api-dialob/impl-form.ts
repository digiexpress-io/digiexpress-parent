import { DialobApi } from './dialob-types';
import { parsePage } from './parsePage';
import { parseInputRow } from './parseInputRow';


export class FormImpl implements DialobApi.Form {
  private _state: Readonly<DialobApi.FormState>; // internal state used by backend
  private _id: string;
  constructor(id: string, state: Readonly<DialobApi.FormState>) {
    this._state = state;
    this._id = id;
  }
  public getItem(id: string): DialobApi.ActionItem | undefined {
    return this.state.items[id];
  }
  public getValueSet(id: string): DialobApi.ActionValueSet | undefined {
    return this.state.valueSets[id];
  }
  public getVariable(id: string): any {
    return this.state.variables[id];
  }

  public get state(): DialobApi.FormState {
    return this._state;
  }
  public withState(next: Readonly<DialobApi.FormState>) {
    return new FormImpl(this._id, next);
  }

  public toInputRow(id: string): DialobApi.ControlInputRow {
    return parseInputRow(id, this);
  }
  
  public toParent(id: string): DialobApi.ActionItem | undefined {
    const [parentId] = this._state.reverseItemMap[id];
    return parentId ? this.getItem(parentId) : undefined;
  }

  public toChildren(id: string): DialobApi.ActionItem[] {
    const target = this.getItem(id);
    return (target?.items ?? [])
      .map(childId => this.getItem(childId))
      .filter(e => e ? true : false)
      .map(e => e as DialobApi.ActionItem);
  }

  public toValueSet(id: string): DialobApi.ActionValueSet | undefined {
    const item = this.getItem(id);
    if(!item) {
      return undefined;
    }

    // value set direct link
    if(item.valueSetId) {
      return this.state.valueSets[item.valueSetId];
    }

    // try resolving using parent item
    const parent = this._state.reverseItemMap[item.id];


    if(parent && parent.size === 1) {
      const valueSetId: string | undefined = parent.values().next().value;
      return valueSetId === undefined ? undefined : this.toValueSet(valueSetId);
    }

    return undefined;
  }
  public toPage(id: string): DialobApi.ControlPage {
    return parsePage(id, this);
  }
  public toErrors(id: string): DialobApi.ActionError[] {
    return this.state.errors[id] ?? [];
  }
  public toDescription(id: string): string | undefined {
    const sessionItem = this.getItem(id)!
    if (!sessionItem.description || sessionItem.description?.trim().length === 0) {
      return undefined;
    }
    // \r\n = windows line break, \n' = unix line break, \u200B unicode zero-width space
    if (sessionItem.description.replace('\r\n', '').replace('\n', '').replace('\u200B', '').trim().length === 0) {
      return undefined;
    }
    return sessionItem.description;
  }
  
  
  public get pages(): readonly DialobApi.ControlPage[] {
    return Object.freeze(this.pagesIds
      .map(id => this.toPage(id))
      .sort((a, b) => a.order - b.order)
    )
      
  }

  public get pagesIds(): readonly string[] {
    const tip = this.tip;
    if(!tip || !tip.items) {
      return Object.freeze([]);
    }
    return Object.freeze(tip.items);
  }

  public get id() { return this._id }

  public get tip(): DialobApi.ActionItem | undefined {
    const item = this.getItem('questionnaire')

    if (!item) {
      return; // not ready yet
    }

    const { activeItem, items } = item;
    if (!activeItem || !items) {
      console.error("can't find active item or any items at all!");
      return;
    }
    return item as DialobApi.ActionItem;
  }

  //public get completed(): boolean { return this.state.complete }
  //public get locale(): string | undefined { return this.state.locale }
  //public get allItems(): ActionItem[] { return Object.values(this.state.items) }
}