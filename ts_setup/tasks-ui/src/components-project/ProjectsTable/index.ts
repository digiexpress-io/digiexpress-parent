

import { TableSubTitle, TableTitle } from './TableTitle';
import { TableGroups } from './TableGroups';
import { SortableHeader, SortableHeaders } from './SortableHeader';

import Users from './CellUsers';
import CreatedDate from './CellCreatedDate';
import Menu from './CellMenu';
import RepoType from './CellRepoType';
import RepoId from './CellRepoId';
import TitleAs from './CellTitle';
import { StyledFillerRows, StyledTableBody } from './StyledTable';
import { TableConfigProps } from './table-ctx';

declare namespace ProjectsTable {
  export type { TableConfigProps };
}


namespace ProjectsTable {
  export const Title = TableTitle;
  export const SubTitle = TableSubTitle;
  export const Groups = TableGroups;
  export const ColumnHeader = SortableHeader;
  export const ColumnHeaders = SortableHeaders;
  export const CellUsers = Users;
  export const CellCreatedDate = CreatedDate;
  export const CellMenu = Menu;
  export const CellTitle = TitleAs;
  export const CellRepoId = RepoId;
  export const CellRepoType = RepoType;
  export const TableFiller = StyledFillerRows;
  export const TableBody = StyledTableBody;
}

export default ProjectsTable;


