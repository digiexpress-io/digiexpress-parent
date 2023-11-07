

import { TableSubTitle, TableTitle } from './TableTitle';
import { TableGroups } from './TableGroups';
import { SortableHeader, SortableHeaders } from './SortableHeader';

import CellFormCreatedDateAs from './CellFormCreatedDate';
import Menu from './CellMenu';
import CellFormTitleAs from './CellFormTitle';
import { StyledFillerRows, StyledTableBody } from './StyledTable';
import { TableConfigProps } from './table-ctx';

declare namespace TenantsTable {
  export type { TableConfigProps };
}


namespace TenantsTable {
  export const Title = TableTitle;
  export const SubTitle = TableSubTitle;
  export const Groups = TableGroups;
  export const ColumnHeader = SortableHeader;
  export const ColumnHeaders = SortableHeaders;
  export const CellFormCreatedDate = CellFormCreatedDateAs;
  export const CellMenu = Menu;
  export const CellFormTitle = CellFormTitleAs;
  export const TableFiller = StyledFillerRows;
  export const TableBody = StyledTableBody;
}

export default TenantsTable;


