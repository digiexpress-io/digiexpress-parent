import { ImmutableTablePagination } from './ImmutableTablePagination';
import { Order, TablePagination } from './table-types';
export type { TablePagination };

declare namespace Pagination {
  export type { TablePagination, Order };
}


namespace Pagination {
  export const TablePaginationImpl = ImmutableTablePagination;
}

export default Pagination;