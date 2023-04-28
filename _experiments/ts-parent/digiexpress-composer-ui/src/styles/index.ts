
import {
  StyledCards,
  StyledCardsProps,
  StyledCardItemProps
} from './StyledCards';

import {
  StyledTreeView
} from './StyledTreeView';

import {
  TabPanelProps,
  StyledTabPanel
} from './StyledTabPanel'


declare namespace Styles {
  export type { StyledCardsProps, StyledCardItemProps };
  export type { TabPanelProps };

}


namespace Styles {
  export const Cards = StyledCards;
  export const TreeView = StyledTreeView;
  export const TabPanel = StyledTabPanel;

}

export default Styles;


