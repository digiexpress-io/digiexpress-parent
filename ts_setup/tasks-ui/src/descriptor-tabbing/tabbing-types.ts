
export type TabId = string;
export type TabSelection = string | number;
export interface TabBody {

}

export interface SelectionOptions {
  disableOthers?: true
}

export interface Tab<I extends TabId, T extends TabBody> {
  id: I;
  body: T;
  selected: TabSelection[];
  active: boolean;
}

export interface Reducer<I extends TabId, B extends TabBody> {
  withTabBody: (tabId: I, newBody: B) => void;
  withTabActivity: (tabId: I, options?: SelectionOptions) => void;
  withTabSelecion: (tabId: I, item: TabSelection, options?: SelectionOptions) =>  void;
}

export interface TabbingContextType<I extends TabId, B extends TabBody> {
  reducer: Reducer<I, B>;
  tabs: Tab<I, B>[];
}