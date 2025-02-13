
export type TabId = string;
export type TabSelection = string | number;
export interface TabBody {

}

export interface SelectionOptions {
  disableOthers?: true | false
}

export interface Tab<I extends TabId, T extends TabBody> {
  id: I;
  body: T;
  selected: readonly TabSelection[];
  active: boolean;
}

export interface SingleTabInit<B extends TabBody> {
  active?: boolean;
  body: B;
}

export type TabbingInit<I extends TabId, B extends TabBody> = Record<I, SingleTabInit<B>>;