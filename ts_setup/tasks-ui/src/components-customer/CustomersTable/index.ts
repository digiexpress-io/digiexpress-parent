

import { TableSubTitle, TableTitle } from './TableTitle';
import { TableGroups } from './TableGroups';
import { SortableHeader, SortableHeaders } from './SortableHeader';

import CreatedDate from './CellCreatedDate';
import Menu from './CellMenu';
import CustomerNameAs from './CellCustomerName';
import { StyledFillerRows, StyledTableBody } from './StyledTable';
import { TableConfigProps } from './table-ctx';

declare namespace CustomersTable {
  export type { TableConfigProps };
}


namespace CustomersTable {
  export const Title = TableTitle;
  export const SubTitle = TableSubTitle;
  export const Groups = TableGroups;
  export const ColumnHeader = SortableHeader;
  export const ColumnHeaders = SortableHeaders;
  export const CellCreatedDate = CreatedDate;
  export const CellMenu = Menu;
  export const CellCustomerName = CustomerNameAs;
  export const TableFiller = StyledFillerRows;
  export const TableBody = StyledTableBody;
}

export default CustomersTable;


