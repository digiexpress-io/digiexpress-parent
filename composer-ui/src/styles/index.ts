
import {
  StyledCards,
  StyledCardsProps,
  StyledCardItemProps
} from './StyledCards';

import {
  StyledTreeView
} from './StyledTreeView';



declare namespace Styles {
  export type { StyledCardsProps, StyledCardItemProps };

}


namespace Styles {
  export const Cards = StyledCards;
  export const TreeView = StyledTreeView;

}

export default Styles;


