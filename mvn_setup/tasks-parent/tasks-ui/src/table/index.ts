import { TablePagination, TablePagination as TablePaginationAs } from './table-pagination';

declare namespace Pagination {
  export type { TablePagination };
}


namespace Pagination {
  export const TablePaginationImpl = TablePaginationAs;

}

export default Pagination;