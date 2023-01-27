import DeClient from '@declient';


function descendingComparator<T>(a: T, b: T, orderBy: keyof T) {
  
  let aValue = a[orderBy];
  let bValue = b[orderBy];

  if( (typeof aValue) === 'string' ) {
    aValue = (aValue as unknown as string).toLowerCase() as any;
    bValue = (bValue as unknown as string).toLowerCase() as any;
  }
  
  
  if (bValue < aValue) {
    return -1;
  }
  if (bValue > aValue) {
    return 1;
  }
  return 0;
}

type Order = 'asc' | 'desc';

function getComparator<Key extends keyof any>(
  order: Order,
  orderBy: Key,
): (
    a: { [key in Key]: number | string },
    b: { [key in Key]: number | string },
  ) => number {
  return order === 'desc'
    ? (a, b) => descendingComparator(a, b, orderBy)
    : (a, b) => -descendingComparator(a, b, orderBy);
}

function stableSort<T>(array: readonly T[], comparator: (a: T, b: T) => number) {
  const stabilizedThis = array.map((el, index) => [el, index] as [T, number]);
  stabilizedThis.sort((a, b) => {
    const aValue = a[0];
    const bValue = b[0];
    
    const order = comparator(aValue, bValue);
    if (order !== 0) {
      return order;
    }
    return a[1] - b[1];
  });
  return stabilizedThis.map((el) => el[0]);
}

class DescriptorPagination {
  private _def: DeClient.ServiceDefinition;
  private _page: number = 0;
  private _rowsPerPage: number = 15;
  private _order: Order = 'asc';
  private _orderBy: keyof DeClient.ServiceDescriptor = 'name';
  private _entries: DeClient.ServiceDescriptor[];
  private _emptyRows: number;

  constructor(init: {
    def: DeClient.ServiceDefinition,
    page?: number,
    rowsPerPage?: number,
    order?: Order;
    orderBy?: keyof DeClient.ServiceDescriptor;
  }) {
    this._def = init.def;
    if (init.page) {
      this._page = init.page;
    }
    if (init.rowsPerPage) {
      this._rowsPerPage = init.rowsPerPage;
    }
    if (init.order !== undefined) {
      this._order = init.order;
    }
    if (init.orderBy) {
      this._orderBy = init.orderBy;
    }

    this._entries = stableSort(this._def.descriptors, getComparator(this._order, this._orderBy))
      .slice(
        this._page * this._rowsPerPage,
        this._page * this._rowsPerPage + this._rowsPerPage);
    this._emptyRows = this._page > 0 ? Math.max(0, (1 + this._page) * this._rowsPerPage - this._def.descriptors.length) : 0;
  }

  withOrderBy(orderBy: keyof DeClient.ServiceDescriptor) {
    const isAsc = orderBy === this._orderBy && this._order === 'asc';
    const order = isAsc ? 'desc' : 'asc';

    return new DescriptorPagination({
      order, orderBy,
      def: this._def,
      rowsPerPage: this._rowsPerPage,
      page: this._page
    });
  }
  withPage(page: number) {
    return new DescriptorPagination({
      page,
      order: this._order,
      orderBy: this._orderBy,
      def: this._def,
      rowsPerPage: this._rowsPerPage
    });
  }
  withRowsPerPage(rowsPerPage: number) {
    return new DescriptorPagination({
      def: this._def,
      order: this._order,
      orderBy: this._orderBy,
      rowsPerPage, page: 0
    });
  }
  get rowsPerPageOptions() {return [15, 40, 80, 120] }
  get entries() { return this._entries }
  get def() { return this._def }
  get page() { return this._page }
  get rowsPerPage() { return this._rowsPerPage }
  get order() { return this._order }
  get orderBy() { return this._orderBy }
  get emptyRows() { return this._emptyRows }
}


export { DescriptorPagination };
export type { Order };
