import {
  StyledCards,
  StyledCardsProps,
  StyledCardItemProps
} from './StyledCards';

import * as TaskTableStyles from './StyledTaskTable';

import StyledSpotlightLabel from './StyledSpotlightLabel';


declare namespace Styles {
  export type { StyledCardsProps, StyledCardItemProps };

}


namespace Styles {
  export const Cards = StyledCards;
  export const SpotlightLabel = StyledSpotlightLabel;
  export const TaskTable = { 
    TableCell: TaskTableStyles.StyledTableCell,
    TableBody: TaskTableStyles.StyledTableBody,
    TableRowEmpty: TaskTableStyles.StyledEmptyTableRow,
    TableHeaderSortable: TaskTableStyles.StyledSortableHeader,
    lineHeight: TaskTableStyles.lineHeight,
    lineHeightLarge: TaskTableStyles.lineHeightLarge,
  }
}

export default Styles;


