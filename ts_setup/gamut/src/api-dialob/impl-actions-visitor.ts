import { DialobApi } from './dialob-types';



export class ActionVisitorError extends Error {
  constructor(reason: string) {
    super(reason);
    Object.setPrototypeOf(this, ActionVisitorError.prototype);
  }
}

export class ActionVisitor {
  private _items: Record<string, DialobApi.ActionItem>;
  private _valueSets: Record<string, DialobApi.ActionValueSet>;
  private _variables: Record<string, any>;
  private _errors: Record<string, DialobApi.ActionError[]>;
  private _reverseItemMap: Record<string, Set<string>>;
  private _locale?: string;
  private _completed: boolean;

  constructor(previous?: DialobApi.FormState) {
    if(previous) {
      this._items = structuredClone(previous.items);
      this._valueSets = structuredClone(previous.valueSets);
      this._variables = structuredClone(previous.variables);
      this._errors = structuredClone(previous.errors);
      this._reverseItemMap = structuredClone(previous.reverseItemMap);
      this._locale = previous.locale;
      this._completed = previous.completed;
    } else {
      this._items = {};
      this._reverseItemMap = {};
      this._valueSets = {};
      this._errors = {};
      this._completed = false;
      this._variables = {};
    }
  }

  public withActions(actions: DialobApi.Action[]): Readonly<DialobApi.FormState> {
    actions.forEach(action => this.vithAction(action));
    return Object.freeze({
      items: Object.freeze(structuredClone(this._items)),
      valueSets: Object.freeze(structuredClone(this._valueSets)),
      variables: Object.freeze(structuredClone(this._variables)),
      errors: Object.freeze(structuredClone(this._errors)),
      reverseItemMap: Object.freeze(structuredClone(this._reverseItemMap)),
      locale: this._locale,
      completed: this._completed
    });
  }

  private withReset(action: { }) {
    this._items = {};
    this._reverseItemMap = {};
    this._valueSets = {};
    this._errors = {};
    this._completed = false;
    this._variables = {};
  }

  private withAnswer(action: {id: string, answer: any}) {
    const answer = this._items[action.id];
    if (!answer) throw new ActionVisitorError(`No item found with id '${action.id}'`);
    if (answer.type === 'questionnaire' || answer.type === 'group' || answer.type === 'surveygroup' || answer.type === 'note') {
      throw new ActionVisitorError(`Item '${action.id}' is not an answer!`);
    }

    answer.value = action.answer;
  }

  private withItem(action: { item: DialobApi.ActionItem }) {
    const item = action.item;
    if (action.item.type === 'context' || action.item.type === 'variable') {
      this._variables[action.item.id] = action.item.value;
      return;
    } 

    this._items[item.id] = item;
    if (!('items' in item && item.items)) {
      return;
    }

    const parentId = item.id;
    const refIds = item.items;
    for (const refId of refIds) {
      if (!this._reverseItemMap[refId]) {
        this._reverseItemMap[refId] = new Set();
      }
      this._reverseItemMap[refId].add(parentId);
    }
  }

  private withError(action: { error: DialobApi.ActionError; }) {
    const error = action.error;
      if (!this._errors[error.id]) {
        this._errors[error.id] = [error];
      } else {
        const itemErrors = this._errors[error.id];
        const errorIdx = itemErrors.findIndex(e => e.code === error.code);
        if (errorIdx !== -1) {
          itemErrors[errorIdx] = error;
        } else {
          itemErrors.push(error);
        }
      }
  }

  private withLocale(action: { value: string }) {
    this._locale = action.value;
  }


  private withValueSet(action: { valueSet: DialobApi.ActionValueSet }) {
    this._valueSets[action.valueSet.id] = action.valueSet;
  }

  private withRemoveItems(action: { ids: string[] }) {
    for (const id of action.ids) {
      delete this._items[id];
      delete this._errors[id];

      if (this._reverseItemMap[id]) {
        this._reverseItemMap[id].forEach(reference => {
          const referencedItem: any = this._items[reference];
          if (!referencedItem || !referencedItem['items']) return;
          const idx = referencedItem.items.indexOf(reference);
          if (idx === -1) return;
          referencedItem.items.splice(idx, 1);
        });
      }
      delete this._reverseItemMap[id];
    }
  }

  private withDeleteRow(action: { id: string }) {
    delete this._items[action.id];
  }

  private withComplete(action: { }) {
    this._completed = true;
  }

  private withRemoveError(action: { error: DialobApi.ActionError }) {
    const error = action.error;
    const itemErrors = this._errors[error.id];
    if (itemErrors) {
      const errorIdx = itemErrors.findIndex(e => e.code === error.code);
      if (errorIdx !== -1) {
        itemErrors.splice(errorIdx, 1);
      }
    }
  }
  private withRemoveValueSets(action: { ids: string[] }) {
    for (const id of action.ids) {
      delete this._valueSets[id];
    }
  }

  private vithAction(action: DialobApi.Action) {
    if (action.type === 'RESET') {
      this.withReset(action)
    } else if (action.type === 'ANSWER') {
      this.withAnswer(action)
    } else if (action.type === 'ITEM') {
      this.withItem(action)
    } else if (action.type === 'ERROR') {
      this.withError(action)
    } else if (action.type === 'LOCALE') {
      this.withLocale(action)
    } else if (action.type === 'VALUE_SET') {
      this.withValueSet(action)
    } else if (action.type === 'REMOVE_ITEMS') {
      this.withRemoveItems(action)
    } else if (action.type === 'ADD_ROW') {
      // Wait for server response
    } else if (action.type === 'DELETE_ROW') {
      this.withDeleteRow(action)
    } else if (action.type === 'COMPLETE') {
      this.withComplete(action)
    } else if (action.type === 'REMOVE_ERROR') {
      this.withRemoveError(action)
    } else if (action.type === 'REMOVE_VALUE_SETS') {
      this.withRemoveValueSets(action)

    } else if (action.type === 'NEXT') {
      // Wait for server response
    } else if (action.type === 'PREVIOUS') {
      // Wait for server response
    } else if (action.type === 'GOTO') {
      // Wait for server response
    } else if (action.type === 'SET_LOCALE') {
      // Wait for server response      
    } else {
      console.warn(`; Received unexpected action type ${(action as DialobApi.Action).type} !`);
    }
  }
}