
export namespace DialobApi {

}


export declare namespace DialobApi {
  /**
   * backend api types
   */

  // backend actions
  export type Action = (
    { type: 'RESET' } |
    { type: 'LOCALE'; value: string; } |
    { type: 'SET_LOCALE'; value: string; } |
    { type: 'REMOVE_ITEMS', ids: string[]; } |
    { type: 'REMOVE_VALUE_SETS', ids: string[]; } |
    { type: 'ANSWER'; id: string; answer: any; } |
    { type: 'ADD_ROW'; id: string; } |
    { type: 'DELETE_ROW'; id: string; } |
    { type: 'PREVIOUS'; } |
    { type: 'NEXT'; } |
    { type: 'GOTO'; id: string; } |
    { type: 'COMPLETE'; } |
    { type: 'ERROR'; error: ActionError; } |
    { type: 'REMOVE_ERROR'; error: ActionError } |
    { type: 'VALUE_SET'; valueSet: ActionValueSet } |
    { type: 'ITEM'; item: ActionItem }
  );

  export type ActionType = Action['type'];


  // dialob form control types
  export type ActionItemType = (
    'questionnaire' 
    | 'group' 
    | 'note' 
    | 'row' 
    | 'rowgroup' 
    | 'context' 
    | 'variable'

  // input types
    | 'date' 
    | 'time' 
    | 'decimal' 
    | 'text' 
    | 'number' 
    | 'boolean' 
    | 'multichoice' 
    | 'survey' 
    | 'surveygroup' 
    | 'list' 
  );


  export interface ActionItem {
    id: string;
    type: ActionItemType;
    view?: string;
    label?: string;
    description?: string;
    disabled?: boolean;
    required?: boolean;
    className?: string[];
    value?: any;
    items?: string[];
    activeItem?: string;
    availableItems?: string[];
    allowedActions?: Array<'ANSWER' | 'NEXT' | 'PREVIOUS' | 'COMPLETE' | 'ADD_ROW' | 'DELETE_ROW'>;
    answered?: boolean;
    valueSetId?: string;
    props?: any;
  }


  export interface ActionError {
    id: string;
    code: string;
    description: string;
  }

  export interface ActionValueSet {
    id: string; 
    entries: { 
      key: string; 
      value: string; 
    }[];
  }

  // Web methods
  export type FetchActionPOST = (sessionId: string, actions: Action[], rev: number) => Promise<Response>
  export type FetchActionGET = (sessionId: string) => Promise<Response>
  export type FetchReviewGET = (sessionId: string) => Promise<Response>

  export interface DialobContextType {
    fetchActionPost: FetchActionPOST;
    fetchActionGet: FetchActionGET;
    fetchReviewGet: FetchReviewGET
    syncWait?: number | undefined;
  }





  /**
   * FORM related
   */
  export type ControlId = string;
  export type ControlLocalizedString = string;

  export type ControlType = (
    string
  );


  export interface ControlPage {
    id: ControlId;
    
    status: 'completed' | 'filling' | 'todo' | 'submit';
    summary: boolean;
    active: boolean;
    
    // can proceed to next page
    next: boolean;

    nextPageId: ControlId | undefined;

    submitted: boolean;
    singular: boolean;
    order: number;
  }


  // top level interface for representing every form element, see inherited concreate members for more details
  export interface ControlInput {
    id: ControlId;
    label: ControlLocalizedString | undefined;
    description: ControlLocalizedString | undefined;
    errors: ActionError[];
    invalid: boolean | undefined;
    source: ActionItem;
  }


  // top level interface for representing every form element, see inherited concreate members for more details
  export interface ControlInputRow {
    id: ControlId;
    source: ActionItem;

    order: number; // index of the row, starts from 0
    total: number; // total rows
  }


  export interface FormState {
    items: Readonly<Record<string, ActionItem>>;
    reverseItemMap: Readonly<Record<string, Set<string>>>;
    valueSets: Readonly<Record<string, ActionValueSet>>;
    errors: Readonly<Record<string, ActionError[]>>;
    locale?: string | undefined;
    completed: boolean;
    variables: Readonly<Record<string, any>>;  
  }



  export interface Form {
    getVariable(id: string): any;
    getItem(id: ControlId): ActionItem | undefined;
    getValueSet(id: string): ActionValueSet | undefined;

    toDescription(id: ControlId): string | undefined;
    toErrors(id: ControlId): ActionError[];
    toPage(id: ControlId): ControlPage;
    toValueSet(id: ControlId): ActionValueSet | undefined;
    toChildren(id: ControlId): ActionItem[];
    toInputRow(id: ControlId): ControlInputRow;
    toParent(id: ControlId): ActionItem | undefined;

    proceedAllowed: boolean;
    completeAllowed: boolean;

    tip: ActionItem | undefined;
    pagesIds: readonly ControlId[];

    pages: readonly ControlPage[];
    id: string;
    state: FormState;
  }


  export interface FormStore {
    id: string;
    form: Form;
    pending: boolean;
    pull(): Promise<void>;

    setAnswer(itemId: string, answer: any): void
    addRowToGroup(rowGroupId: string): void
    deleteRow(rowId: string): void

    complete(): void
    next(): void;
    previous(): void;

    goToPage(pageId: string): void;
    setLocale(locale: string): void;
  }

  export interface FormContextType {
    store: DialobApi.FormStore;
    variant: string;
    onAfterComplete: () => void;
  }
}
