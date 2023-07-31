import {
  StyledCards,
  StyledCardsProps,
  StyledCardItemProps
} from './StyledCards';

import {
  lineHeight,
  lineHeightLarge,
  StyledTableBody,
  StyledTableCell,
  StyledFillerRows
} from './StyledTable';


import { StyledProgressBar } from './StyledProgressBar';

declare namespace Styles {
  export type { StyledCardsProps, StyledCardItemProps };
}


namespace Styles {
  export const Cards = StyledCards;
  export const ProgressBar = StyledProgressBar;
  export const TableBody = StyledTableBody;
  export const TableFiller = StyledFillerRows;
  export const TableCell = StyledTableCell;
  export const TableConfig = {
    lineHeight, lineHeightLarge
  }
}

export default Styles;


