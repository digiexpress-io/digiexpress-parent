import React from 'react';
import { Box, Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import { useIntl } from 'react-intl'

import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";

import { Composer } from '../../context';
import DecisionItem from './DecisionItem';
import TreeViewToggle from '../TreeViewToggle';
import {useUtilityClasses, DecisionsListRoot} from './useUtilityClasses';
import { useWrenchNav } from '../../nav';

const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}


export const DecisionsList: React.FC<{}> = () => {
  const intl = useIntl();
  const { session } = Composer.useComposer();
  const classes = useUtilityClasses();
  const { onNav, getDecisions } = useWrenchNav();
  const expanded: string[] = getDecisions().expanded ?? [];

  function handleExpanded(_event: React.SyntheticEvent, nodeIds: string[]) {
    const nodes = new TreeViewToggle().onNodeToggle(nodeIds);
    onNav({ type: 'DECISIONS', id: nodes.main, expanded: [...nodes.expanded] })
  }

  return (
    <DecisionsListRoot className={classes.root}>
      <Typography className={classes.title} variant='h1'>{intl.formatMessage({ id: 'main.decisions.all' })}</Typography>

      <SimpleTreeView expandedItems={expanded}
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
        onExpandedItemsChange={handleExpanded}
      >
        { Object.values(session.site.decisions)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)) )
          .map(decision => (<DecisionItem key={decision.id} decisionId={decision.id} />))
        }
      </SimpleTreeView>
    </DecisionsListRoot>
  );
}


