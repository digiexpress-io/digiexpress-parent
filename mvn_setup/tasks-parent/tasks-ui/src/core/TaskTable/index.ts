import {
  TableSubTitle,
  TableTitle
} from './TableTitle';

import {
  TableGroups, RenderProps, TableProps
} from './TableGroups';


import { SortableHeader, SortableHeaders } from './SortableHeader';


import Assignees from './CellAssignees';
import DueDate from './CellDueDate';
import Menu from './CellMenu';
import Roles from './CellRoles';
import TitleAs from './CellTitle';
import Priority from './CellPriority';
import Status from './CellStatus';


declare namespace TaskTable {
  export type { RenderProps, TableProps };
}


namespace TaskTable {
  export const Title = TableTitle;
  export const SubTitle = TableSubTitle;
  export const Groups = TableGroups;
  export const ColumnHeader = SortableHeader;
  export const ColumnHeaders = SortableHeaders;
  export const CellAssignees = Assignees;
  export const CellDueDate = DueDate;
  export const CellMenu = Menu;
  export const CellRoles = Roles;
  export const CellTitle = TitleAs;
  export const CellPriority = Priority;
  export const CellStatus = Status;
}

export default TaskTable;

