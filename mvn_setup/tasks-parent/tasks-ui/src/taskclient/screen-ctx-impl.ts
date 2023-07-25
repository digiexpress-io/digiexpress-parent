import { ScreenState, ScreenMutatorBuilder } from './screen-ctx-types';




interface ExtendedInit extends ScreenState {
}

class ScreenStateBuilder implements ScreenMutatorBuilder {
  private _width: number;
  private _height: number;
  private _load: boolean;

  constructor(init: ExtendedInit) {
    this._width = init.width;
    this._height = init.height;
    this._load = init.load;
  }
  get width(): number { return this._width };
  get height(): number { return this._height };
  get load(): boolean { return this._load };

  withScreen(newValues: { width: number, height: number }): ScreenStateBuilder {
    return new ScreenStateBuilder({
      ...this.clone(),
      width: newValues.width,
      height: newValues.height,
      load: false
    });
  }
  withLoad(value: boolean): ScreenStateBuilder {
    return new ScreenStateBuilder({
      ...this.clone(),
      load: value
    });
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      width: init.width,
      height: init.height,
      load: init.load
    }
  }
}


export { ScreenStateBuilder };
export type { };
