import { Tab, TabId, TabBody, SelectionOptions, TabSelection, SingleTabInit, TabbingInit } from './tabbing-types';
import { ImmutableTab } from './ImmutableTab';

export type WithTabBody<I extends TabId, B extends TabBody> = (tabId: I, setter: (oldBody: B) => B) => void;
export type WithTabActivity<I extends TabId> = (tabId: I, options?: SelectionOptions) => void;
export type WithTabSelecion<I extends TabId> = (tabId: I, item: TabSelection, options?: SelectionOptions) =>  void;
export type SetTabs<I extends TabId, B extends TabBody> = React.Dispatch<React.SetStateAction<readonly ImmutableTab<I, B>[]>>;

export function initTabs<I extends TabId, B extends TabBody>(tabbing: TabbingInit<I, B>): readonly ImmutableTab<I, B>[] {
  return Object
    .entries(tabbing)
    .map((props) => {
      const id: I = props[0] as I;
      const init: SingleTabInit<B> = props[1] as SingleTabInit<B>;
      return new ImmutableTab<I, B>({ id, body: init.body, active: init.active });
    });
}

export function tabBodyReducer<I extends TabId, B extends TabBody>(tabId: I, setter: (old: B) => B, setTabs: SetTabs<I, B>): void {
  setTabs(old => {
    //const target = old.find(tab => tab.id === tabId)


    const next: ImmutableTab<I, B>[] = [];
    for(const item of old) {
      if(item.id === tabId) {
        next.push(item.withBody(setter(item.body)));
      } else {
        next.push(item);
      }
    }
    return Object.freeze(next);
  });
}

export function tabActivityReducer<I extends TabId, B extends TabBody>(tabId: I, options: SelectionOptions | undefined, setTabs: SetTabs<I, B>): void {
  setTabs(old => {

    const target = old.find(tab => tab.id === tabId)
    if(!target || target.active) {
      return old;
    }

    const next: ImmutableTab<I, B>[] = [];
    for(const item of old) {
      if(item.id === tabId) {
        next.push(item.withActive(true));
      } else if(item.active) {
        next.push(item.withActive(false));
      } else {
        next.push(item);
      }
    }
    return Object.freeze(next);
  });
}

export function tabSelectionReducer<I extends TabId, B extends TabBody>(tabId: I, tabSelection: TabSelection, options: SelectionOptions | undefined, setTabs: SetTabs<I, B>): void {
  setTabs(old => {
    const next: ImmutableTab<I, B>[] = [];
    for(const item of old) {
      if(item.id === tabId) {
        next.push(item.withSelected([tabSelection], options));
      } else {
        next.push(item);
      }
    }
    return Object.freeze(next);
  });
}