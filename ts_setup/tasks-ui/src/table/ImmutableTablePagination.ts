import { Order, TablePagination, DataType } from './table-types';
import { getComparator, stableSort } from './table-comparators';


export class ImmutableTablePagination<T extends DataType> implements TablePagination<T> {
  private _page: number = 0;
  private _rowsPerPage: number = 5;
  private _order: Order = 'asc';
  private _orderBy: keyof T;
  private _entries: T[];
  private _src: T[];
  private _emptyRows: number;

  constructor(init: {
    src: T[],
    sorted: boolean,
    page?: number,
    rowsPerPage?: number,
    order?: Order;
    orderBy: keyof T;
  }) {
    if (init.page) {
      this._page = init.page;
    }
    if (init.rowsPerPage) {
      this._rowsPerPage = init.rowsPerPage;
    }
    if (init.order !== undefined) {
      this._order = init.order;
    }
    this._src = init.src;
    this._orderBy = init.orderBy;

    const start = this._page * this._rowsPerPage;
    const end = this._page * this._rowsPerPage + this._rowsPerPage;

    if (init.sorted) {
      const comparator: (a: T, b: T) => number = getComparator<T>(this._order, this._orderBy);
      const entries = stableSort<T>(init.src, comparator);
      this._src = entries;
      this._entries = entries.slice(start, end);
    } else {
      this._entries = init.src.slice(start, end);
    }
    this._emptyRows = this._rowsPerPage - this._entries.length;
  }

  withOrderBy(orderBy: keyof T): ImmutableTablePagination<T> {
    const isAsc = orderBy === this._orderBy && this._order === 'asc';
    const order = isAsc ? 'desc' : 'asc';

    return new ImmutableTablePagination({
      sorted: true,
      src: this._src,
      order, orderBy,
      rowsPerPage: this._rowsPerPage,
      page: this._page
    });
  }
  withPage(page: number): ImmutableTablePagination<T> {
    return new ImmutableTablePagination({
      page,
      sorted: false,
      src: this._src,
      order: this._order,
      orderBy: this._orderBy,
      rowsPerPage: this._rowsPerPage
    });
  }
  withRowsPerPage(rowsPerPage: number): ImmutableTablePagination<T> {
    return new ImmutableTablePagination({
      sorted: false,
      src: this._src,
      order: this._order,
      orderBy: this._orderBy,
      rowsPerPage, page: 0
    });
  }
  withSrc(src: T[]): ImmutableTablePagination<T> {
    return new ImmutableTablePagination({
      src,
      page: this._page,
      sorted: true,
      order: this._order,
      orderBy: this._orderBy,
      rowsPerPage: this._rowsPerPage
    });
  }
  get rowsPerPageOptions() { return [5, 15, 40, 80, 120] }
  get entries() { return this._entries }
  get src() { return this._src }
  get page() { return this._page }
  get rowsPerPage() { return this._rowsPerPage }
  get order() { return this._order }
  get orderBy() { return this._orderBy }
  get emptyRows() { return this._emptyRows }
}

