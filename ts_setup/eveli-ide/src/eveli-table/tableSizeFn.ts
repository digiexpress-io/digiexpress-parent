import { Column, Table, VisibilityState } from "@tanstack/react-table";


function getRestoreSize(col: Column<any, unknown>) {
  return col.columnDef.size ?? col.columnDef.minSize ?? 100;
}


type Resize = Record<string, { 
  from: number; 
  to: number;
  isMinLimitReached: boolean;
}>;

class TableSizeVisitor {

  private _table: Table<any>;
  private _delta_for_removed: Record<string, boolean> = {};
  private _delta_for_added: Record<string, boolean> = {};
  private _size_for_decrement: number;
  private _size_of_current_table: number;
  private _size_up_for_grabs: number;
  private _columns_visible: Column<any, unknown>[];
  private _columns_added: string[];
  private _resized: Resize = {};


  constructor(
    table: Table<any>,
    state: {
      prev: VisibilityState,
      next: VisibilityState,
    }) {

      this._table = table;
      Object.entries(state.next).forEach(([key, value]) => {
        if (state.prev[key] !== value && value === false) {
          this._delta_for_removed[key] = value;
        } else if (state.prev[key] !== value && value === true) {
          this._delta_for_added[key] = value;
        }
      })

      this._size_for_decrement = table.getAllFlatColumns()
        .filter(col => !!this._delta_for_added[col.id])
        .map(getRestoreSize)
        .reduce<number>((total, next) => total + next, 0);

      this._size_of_current_table = table.getAllFlatColumns()
        .map(col => col.getSize())
        .reduce<number>((total, next) => total + next, 0);

      this._size_up_for_grabs = table.getAllFlatColumns()
        .filter(col => this._delta_for_removed[col.id] === false)
        .map(col => col.getSize())
        .reduce<number>((total, next) => total + next, 0);

      this._columns_visible = table.getAllFlatColumns().filter(col => {
        if (col.getCanHide()) {
          return state.next[col.id];
        }
        return true;
      });

      this._columns_added = Object.keys(this._delta_for_added); 
  }

  visit() {
    this.visitIncrementColumns();
    this.visitDecrementColumns();

    const result = Object.entries(this._resized).reduce<Record<string, number>>((collector, [key, { to }]) => {
      collector[key] = to;
      return collector;
    }, {});

    return result;
  }

  private visitIncrementColumns() {
    const increment_each_by = Math.ceil(this._size_up_for_grabs / this._columns_visible.length);

    this._columns_visible.reduce<Resize>((sized, col) => {
      sized[col.id] = {
        to: col.getSize() + increment_each_by,
        from: col.getSize(),
        isMinLimitReached: getRestoreSize(col) === col.getSize()
      };
      return sized;
    }, this._resized);
  }

  private visitDecrementColumns() {
    Object.keys(this._delta_for_added).forEach(key => this.visitRestoreColumn(key));
  }

  private visitRestoreColumn(id: string) {
    let needed = getRestoreSize(this._table.getColumn(id)!);


    while(needed > 0) {
      const targets = this._columns_visible
        .filter(col => !this._columns_added.includes(col.id))
        .filter(col => !this._resized[col.id].isMinLimitReached);;
      
      if(targets.length === 0) {
        // can't get it from nowhere
        return;
      }
      const perColumn = Math.ceil(needed/targets.length);
      needed -= this.visitDecrementColumn(perColumn).totalRemoved;
    }
  }

  private visitDecrementColumn(removePerColumn: number) {
    let totalRemoved = 0;

    const targets = this._columns_visible
      .filter(col => !this._columns_added.includes(col.id))
      .filter(col => !this._resized[col.id].isMinLimitReached);

    targets.forEach((col) => {
      const current = this._resized[col.id];
      const removed = Math.max(getRestoreSize(col), removePerColumn);
      totalRemoved += removed;

      this._resized[col.id] = {
        from: col.getSize(),
        to: current.to - removed,
        isMinLimitReached: getRestoreSize(col) === col.getSize()
      }
    });


    return { totalRemoved }
  }

}


export function tableSizeFn(table: Table<any>, prev: Record<string, boolean>, next: Record<string, boolean>): Record<string, number> {
  return new TableSizeVisitor(table, { prev, next }).visit();
}