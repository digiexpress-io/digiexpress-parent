import { SiteApi } from '../api-site';

function descendingComparator<T>(a: T, b: T, orderBy: keyof T) {
  if (b[orderBy] < a[orderBy]) {
    return -1;
  }
  if (b[orderBy] > a[orderBy]) {
    return 1;
  }
  return 0;
}

function getComparator<Key extends keyof any>(
  order: GFeedbackTableArticleOrder,
  orderBy: Key,
): (
  a: { [key in Key]: number | string },
  b: { [key in Key]: number | string },
) => number {
  return order === 'desc'
    ? (a, b) => descendingComparator(a, b, orderBy)
    : (a, b) => -descendingComparator(a, b, orderBy);
}

const MIN_ROWS = 5;

export type GFeedbackTableArticleOrder = 'asc' | 'desc';
export type GFeedbackTableArticleReducerProps = [GFeedbackTableArticleReducer, React.Dispatch<React.SetStateAction<GFeedbackTableArticleReducer>>];
export class GFeedbackTableArticleReducer {
  private _order: GFeedbackTableArticleOrder;
  private _orderBy: keyof SiteApi.Feedback;
  private _page: number;
  private _rowsPerPage: number;
  private _data: SiteApi.Feedback[];
  private _emptyRows: number;
  private _visibleRows: SiteApi.Feedback[];

  public constructor(props: {
    data: SiteApi.Feedback[],
    order: GFeedbackTableArticleOrder | undefined,
    orderBy: keyof SiteApi.Feedback,
    page: number | undefined,
    rowsPerPage: number | undefined,
  }) {
    this._order = props.order ?? 'asc';
    this._orderBy = props.orderBy ?? 'labelValue';
    this._page = props.page ?? 0;;
    this._rowsPerPage = props.rowsPerPage ?? MIN_ROWS; 
    this._data = props.data;

    this._emptyRows = Math.max(0, (1 + this._page) * this._rowsPerPage - this._data.length);
    this._visibleRows = [...this._data]
      .sort(getComparator(this._order, this._orderBy) as any)
      .slice(this._page * this._rowsPerPage, this._page * this._rowsPerPage + this._rowsPerPage)
  }

  get rowsPerPage() { return this._rowsPerPage }
  get data() { return this._data; }
  get page() { return this._page; }
  get emptyRows() { return this._emptyRows; }
  get visibleRows() { return this._visibleRows; }
  get order() { return this._order; }
  get orderBy() { return this._orderBy; }

  withRowsPerPage(value: string): GFeedbackTableArticleReducer {
    const rowsPerPage = parseInt(value, 10);
    const page = 0;
    return new GFeedbackTableArticleReducer({
      order: this._order,
      orderBy: this._orderBy,
      data: this._data,
      page: page,
      rowsPerPage: rowsPerPage
    });
  }

  withPage(value: number) {
    return new GFeedbackTableArticleReducer({
      order: this._order,
      orderBy: this._orderBy,
      data: this._data,
      page: value,
      rowsPerPage: this._rowsPerPage
    });
  }

  withData(data: SiteApi.Feedback[]) {
    return new GFeedbackTableArticleReducer({
      order: this._order,
      orderBy: this._orderBy,
      data: data,
      page: this._page,
      rowsPerPage: this._rowsPerPage
    });
  }

  withOrderBy(property: keyof SiteApi.Feedback, userOrder?: GFeedbackTableArticleOrder | undefined): GFeedbackTableArticleReducer {
    const startOrder = userOrder ?? this._order; 

    const isAsc = this._orderBy === property && startOrder === 'asc';
    const order = (isAsc ? 'desc' : 'asc');
    
    return new GFeedbackTableArticleReducer({
      order: order,
      orderBy: property,
      data: this._data,
      page: this._page,
      rowsPerPage: this._rowsPerPage
    });
  }
}