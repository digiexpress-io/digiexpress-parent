

export interface ScreenContextType {
  setState: ScreenDispatch;
  state: ScreenState;
}

export type ScreenMutator = (prev: ScreenMutatorBuilder) => ScreenMutatorBuilder;
export type ScreenDispatch = (mutator: ScreenMutator) => void;

export interface ScreenState {
  width: number;
  height: number;
  load: boolean;
}


export interface ScreenMutatorBuilder extends ScreenState {
  withScreen(newValues: { width: number, height: number }): ScreenMutatorBuilder;
  withLoad(load: boolean): ScreenMutatorBuilder;
}
