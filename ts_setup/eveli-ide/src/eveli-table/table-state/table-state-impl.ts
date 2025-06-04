import { SortingState, VisibilityState, ColumnFiltersState, ColumnSizingState, OnChangeFn, Updater, PaginationState } from "@tanstack/react-table";
import { TableState } from "./table-state-types";
import React from "react";
import { Md5 } from 'ts-md5';
import { useFetch } from "@dxs-ts/eveli-fetch";
import { useIam } from "@/api-iam";
import { useTenantConfig, useTenantConfigFeatures } from "@/api-tenant-config";



class TableStateImpl implements TableState {

  private _onNext: (next: TableState) => Promise<void>;
  private _sorting: SortingState;
  private _pagination: PaginationState;
  private _columnVisibility: VisibilityState;
  private _columnFilters: ColumnFiltersState;
  private _columnSizing: ColumnSizingState;
  private _filterDialogOpen: boolean;
  private _hash: string;

  constructor(props: {
    sorting: SortingState;
    pagination: { pageIndex: number; pageSize: number; };
    columnVisibility: VisibilityState;
    columnFilters: ColumnFiltersState;
    columnSizing: ColumnSizingState;
    filterDialogOpen: boolean;
    onNext: (next: TableState) => Promise<void>
  }) {
    this._sorting = props.sorting;
    this._pagination = props.pagination;
    this._columnVisibility = props.columnVisibility;
    this._columnFilters = props.columnFilters;
    this._columnSizing = props.columnSizing;
    this._filterDialogOpen = props.filterDialogOpen;
    this._onNext = props.onNext;

    this._hash = new Md5()
      .appendStr(JSON.stringify(props.sorting))
      .appendStr(JSON.stringify(props.pagination))
      .appendStr(JSON.stringify(props.columnVisibility))
      .appendStr(JSON.stringify(props.columnFilters))
      .appendStr(JSON.stringify(props.columnSizing))
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
      filterDialogOpen: this._filterDialogOpen
    }
  }

  public restore(props: {
    sorting: SortingState;
    pagination: { pageIndex: number; pageSize: number; };
    columnVisibility: VisibilityState;
    columnFilters: ColumnFiltersState;
    columnSizing: ColumnSizingState;
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
    filterDialogOpen: false,
    onNext: props.onNext
  });
}



export function useTableState(props: { initialPageSize: number }) {
  const settigsId = 'tables';
  const dataId = 'table-state';

  const { user } = useIam();
  const { isEnabled } = useTenantConfigFeatures()
  const isSmartTables = isEnabled('SMART_TABLES');
  const [loading, setLoading] = React.useState(true);
  const [lastSync, setLastSync] = React.useState<string>();
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {});
  const onNext: (next: TableState) => Promise<void> = React.useCallback(async (next) => {

    setLastSync(prev => {
      if(!prev || next?.hash === prev) { 
        return next.hash;
      }
      if(isSmartTables) {
        restApi().updateUiSettings({
          commandType: 'UpsertUiSettings',
          settingsId: settigsId,
          userId: user.userId,
          visibility: [],
          config: [{
            dataId: dataId,
            value: JSON.stringify(next.copy())
          }]
        })
      }
      return next.hash;
    })
  }, []);
  const state = React.useState(initTableState({ initialPageSize: props.initialPageSize, onNext }));



  React.useEffect(() => {
    if(!loading || !isSmartTables) {
      setLoading(false);
      return;
    }

    restApi().findUiSettings(settigsId).then(data => {
      if(!data?.config) {
        return;
      }
      try {
        const config = data.config.find(config => config.dataId === dataId);
        if(config) {
          state[1]((prev) => prev.restore(JSON.parse(config.value)));
        }
      } catch(e) {
        console.error('Failed to parse table settings', e)
      }
      setLoading(false);
    });

  }, [loading, isSmartTables]);

  return { state, loading };
}