import { Popper } from './popper-types';


export class ImmutablePopper implements Popper {
  private _popperOpen: boolean;
  private _popperId?: string;
  private _anchorEl?: HTMLElement;

  constructor(init: Popper) {
    this._popperOpen = init.popperOpen;
    this._anchorEl = init.anchorEl;
    this._popperId = init.popperId;
  }
  withPopperOpen(popperId: string, popperOpen: boolean, anchorEl?: HTMLElement): ImmutablePopper {
    if (popperOpen && !anchorEl) {
      throw new Error("anchor must be defined when opening popper");
    }
    if (popperId !== this._popperId && anchorEl) {
      return new ImmutablePopper({ popperId, popperOpen: true, anchorEl });
    }

    return new ImmutablePopper({ popperId, popperOpen, anchorEl });
  }
  withPopperToggle(popperId: string, anchorEl?: HTMLElement): ImmutablePopper {
    return this.withPopperOpen(popperId, !this._popperOpen, anchorEl);
  }
  get popperId() { return this._popperId }
  get popperOpen() { return this._popperOpen }
  get anchorEl() { return this._anchorEl }
}


export function initPopper(): Popper {
  return new ImmutablePopper({ popperOpen: false });
}
