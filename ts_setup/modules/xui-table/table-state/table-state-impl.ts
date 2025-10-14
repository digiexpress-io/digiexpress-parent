import { SortingState, VisibilityState, ColumnFiltersState, ColumnSizingState, Updater, PaginationState, ColumnOrderState, ColumnDef } from "@tanstack/react-table";
import { TableState, TableStateInitWith } from "./table-state-types";
import React from "react";
import { Md5 } from 'ts-md5';
import { useLastTableState } from "./last-table-state";



class TableStateImpl implements TableState {

  private _onNext: (next: TableState) => Promise<void>;
  private _sorting: SortingState;
  private _pagination: PaginationState;
  private _columnVisibility: VisibilityState;
  private _columnFilters: ColumnFiltersState;
  private _columnSizing: ColumnSizingState;
  private _columnOrder: ColumnOrderState;
  private _filterDialogOpen: boolean;
  private _hash: string;

  constructor(props: {
    sorting: SortingState;
    pagination: { pageIndex: number; pageSize: number; };
    columnVisibility: VisibilityState;
    columnFilters: ColumnFiltersState;
    columnSizing: ColumnSizingState;
    columnOrder: ColumnOrderState;
    filterDialogOpen: boolean;
    onNext: (next: TableState) => Promise<void>
  }) {
    this._sorting = props.sorting;
    this._pagination = props.pagination;
    this._columnVisibility = props.columnVisibility;
    this._columnFilters = props.columnFilters;
    this._columnSizing = props.columnSizing;
    this._filterDialogOpen = props.filterDialogOpen;
    this._columnOrder = props.columnOrder;
    this._onNext = props.onNext;

    this._hash = new Md5()
      .appendStr(JSON.stringify(props.sorting))
      .appendStr(JSON.stringify(props.pagination))
      .appendStr(JSON.stringify(props.columnVisibility))
      .appendStr(JSON.stringify(props.columnFilters))
      .appendStr(JSON.stringify(props.columnSizing))
      .appendStr(JSON.stringify(props.columnOrder))
      .appendStr(JSON.stringify(props.filterDialogOpen))
      .end() + '';
  }
  public get sorting(): SortingState {
    return this._sorting;
  }
  public get pagination(): { pageIndex: number; pageSize: number; } {
    return this._pagination;
  }
  public get columnVisibility(): VisibilityState {
    return this._columnVisibility;
  }
  public get columnFilters(): ColumnFiltersState {
    return this._columnFilters;
  }
  public get columnSizing(): ColumnSizingState {
    return this._columnSizing;
  }
  public get columnOrder(): ColumnOrderState {
    return this._columnOrder;
  }
  public get filterDialogOpen(): boolean {
    return this._filterDialogOpen;
  }
  public get hash(): string {
    return this._hash;
  }

  private toProps() {
    return {
      onNext: this._onNext,
      ...this.copy()
    }
  }

  public copy() {
    return {
      sorting: this._sorting,
      pagination: this._pagination,
      columnVisibility: this._columnVisibility,
      columnFilters: this._columnFilters,
      columnSizing: this._columnSizing,
      filterDialogOpen: this._filterDialogOpen,
      columnOrder: this._columnOrder
    }
  }

  public restore(props: {
    sorting: SortingState;
    pagination: { pageIndex: number; pageSize: number; };
    columnVisibility: VisibilityState;
    columnFilters: ColumnFiltersState;
    columnSizing: ColumnSizingState;
    columnOrder: ColumnOrderState;
    filterDialogOpen: boolean;
  }): TableState {

    const state = new TableStateImpl({ ...this.toProps(), ...props });
    return state;
  }

  public setSorting(updater: Updater<SortingState>): TableState {
    const prev = this._sorting;
    const sorting = typeof updater === 'function' ? updater(prev) : updater;
    const state = new TableStateImpl({ ...this.toProps(), sorting });
    this._onNext(state);
    return state;
  }
  public setPagination(updater: Updater<PaginationState>): TableState {
    const prev = this._pagination;
    const pagination = typeof updater === 'function' ? updater(prev) : updater;
    const state = new TableStateImpl({ ...this.toProps(), pagination });
    this._onNext(state);
    return state;
  }
  public setColumnVisibility(updater: Updater<VisibilityState>): TableState {
    const prev = this._columnVisibility;
    const columnVisibility = typeof updater === 'function' ? updater(prev) : updater;
    const state = new TableStateImpl({ ...this.toProps(), columnVisibility });
    this._onNext(state);
    return state;
  }
  public setColumnFilters(updater: Updater<ColumnFiltersState>): TableState {
    const prev = this._columnFilters;
    const columnFilters = typeof updater === 'function' ? updater(prev) : updater;
    const state = new TableStateImpl({ ...this.toProps(), columnFilters });
    this._onNext(state);
    return state;
  }
  public setColumnSizing(updater: Updater<ColumnSizingState>): TableState {
    const prev = this._columnSizing;
    const columnSizing = typeof updater === 'function' ? updater(prev) : updater;
    const state = new TableStateImpl({ ...this.toProps(), columnSizing });
    this._onNext(state);
    return state;
  }
  public setColumnOrder(updater: Updater<ColumnOrderState>): TableState {
    const prev = this._columnOrder;
    const columnOrder = typeof updater === 'function' ? updater(prev) : updater;
    const state = new TableStateImpl({ ...this.toProps(), columnOrder });
    this._onNext(state);
    return state;
  }
  public setFilterDialogOpen(filterDialogOpen: boolean): TableState {
    const state = new TableStateImpl({ ...this.toProps(), filterDialogOpen });
    this._onNext(state);
    return state;
  }

  public clear(): TableState {
    const state = new TableStateImpl({ 
      ...this.toProps(),
      sorting: [],
      columnVisibility: {},
      columnFilters: [],
      columnSizing: {},
      columnOrder: []
    });
    this._onNext(state);
    return state;
  }

  public clearFiltersAndVisibility(): TableState {
    const state = new TableStateImpl({ 
      ...this.toProps(),
      columnVisibility: {},
      columnFilters: [],
    });
    this._onNext(state);
    return state;
  }

  public isActive(init: TableStateInitWith): boolean {
    const currentFilterValue = JSON.stringify(this.copy());
    return JSON.stringify(init) === currentFilterValue
  }
}

function initTableState(props: { 
  initialPageSize: number ,
  onNext: (next: TableState) => Promise<void>
}): TableState {
  return new TableStateImpl({
    sorting: [],
    pagination: {pageIndex: 0, pageSize: props.initialPageSize },
    columnVisibility: {},
    columnFilters: [],
    columnSizing: {},
    columnOrder: [],
    filterDialogOpen: false,
    onNext: props.onNext
  });
}



export function useTableState(props: { initialPageSize: number, tableId: string, columns: ColumnDef<any, unknown>[], }) {
  
  const [loading, setLoading] = React.useState(true);
  const lastTableState = useLastTableState(props.tableId);

  const state = React.useState(() => initTableState({ 
    initialPageSize: props.initialPageSize, 
    onNext: lastTableState.onNext 
  }));

  React.useEffect(() => {
    if(!loading) {
      setLoading(false);
      return;
    }
    lastTableState.onRestore().then(config => {
      if(config) {
        state[1]((prev) => prev.restore(config));
      }
      setLoading(false);
    })
  }, [loading]);

  return { state, loading };
}