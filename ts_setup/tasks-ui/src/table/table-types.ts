export type Order = 'asc' | 'desc';
export type DataType = {}

export interface TablePagination<T extends DataType> {
  withOrderBy: (orderBy: keyof T) => TablePagination<T>;
  withPage: (page: number) => TablePagination<T>;
  withRowsPerPage: (rowsPerPage: number) => TablePagination<T>;
  withSrc: (src: T[]) => TablePagination<T>;

  rowsPerPageOptions: number[];
  entries: T[];
  src: T[];
  page: number;
  rowsPerPage: number;
  order: Order;
  orderBy: keyof T;
  emptyRows: number;
}

export interface TablesReducer<T extends DataType> {
  withOrderBy: (tableId: string, orderBy: keyof T) => void;
  withPage: (tableId: string, page: number) => void;
  withRowsPerPage: (tableId: string, rowsPerPage: number) => void;
  withSrc: (tableId: string, src: T[]) => void;
}