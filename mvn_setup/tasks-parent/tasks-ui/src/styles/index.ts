import {
  StyledCards,
  StyledCardsProps,
  StyledCardItemProps
} from './StyledCards';
import { StyledProgressBar } from './StyledProgressBar';
import * as TaskTableStyles from './StyledTaskTable';

import StyledSpotlightLabel from './StyledSpotlightLabel';
import StyledLayout from './StyledLayout';

declare namespace Styles {
  export type { StyledCardsProps, StyledCardItemProps };

}


namespace Styles {
  export const Cards = StyledCards;
  export const SpotlightLabel = StyledSpotlightLabel;
  export const Layout = StyledLayout;
  export const TaskTable = { 
    TableCell: TaskTableStyles.StyledTableCell,
    TableBody: TaskTableStyles.StyledTableBody,
    TableRowEmpty: TaskTableStyles.StyledEmptyTableRow,
    TableHeaderSortable: TaskTableStyles.StyledSortableHeader,
    lineHeight: TaskTableStyles.lineHeight,
    lineHeightLarge: TaskTableStyles.lineHeightLarge,
  }
  export const ProgressBar = StyledProgressBar;
}

export default Styles;


