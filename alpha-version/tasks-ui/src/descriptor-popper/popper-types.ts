
export interface Popper {
  popperOpen: boolean;
  popperId?: string;
  anchorEl?: HTMLElement;
}


export interface PopperContextType {
  state: Popper;
  withPopperOpen: (popperId: string, popperOpen: boolean, anchorEl?: HTMLElement) => void;
  withPopperToggle: (popperId: string, anchorEl?: HTMLElement) => void;
}