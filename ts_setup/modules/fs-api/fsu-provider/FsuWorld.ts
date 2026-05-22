import { Fs } from "../fs-types";

export interface FsuChange {
  id: string;
  isChanged: boolean;
  bodyType: Fs.BodyType;
  getCurrentProps(): { bodyType: Fs.BodyType; id: string, changes: Record<string, any> };
}

export class FsuWorld {
  private _changes: Record<string, FsuChange>;

  constructor(props?: {
    changes: Record<string, FsuChange>
  }) {

    this._changes = props?.changes || {};
  }

  public isChange(id: string): boolean {
    return !!this._changes[id];
  }

  public getChange(id: string): FsuChange {
    const change = this._changes[id];
    if(change) {
      return change;
    }
    throw new Error("Change not created!");
  }

  public withNewChange(init: () => FsuChange): [FsuWorld, string] {
    const created = init();
    const change = this._changes[created.id];
    if(change) {
      throw new Error("Change already created!");
    }
    const changes = { ...this._changes };
    changes[created.id] = created;
    return [new FsuWorld({ changes }), created.id];
  }

  public withChange<T extends FsuChange>(id: string, callback: (prev: T) => T): FsuWorld {
    const prev = this.getChange(id) as T;
    const next = callback(prev);
    const changes = { ...this._changes };
    changes[next.id] = next;
    return new FsuWorld({ changes });
  }
}
