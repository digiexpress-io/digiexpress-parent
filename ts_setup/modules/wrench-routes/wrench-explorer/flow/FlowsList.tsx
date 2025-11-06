import React from 'react';
import { Box, Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import { ArrowDropDown as ArrowDropDownIcon } from "@mui/icons-material";
import { useIntl } from 'react-intl'


import { WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import FlowItem from './FlowItem';
import TreeViewToggle from '../TreeViewToggle';
import { useUtilityClasses, FlowsListRoot } from './useUtilityClasses';
import { useWrenchNav } from '../../wrench-nav';

const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}


export const FlowsList: React.FC<{}> = () => {
  const intl = useIntl();
  const { session } = Composer.useComposer();
  const classes = useUtilityClasses();

  const { getFlows, onNav } = useWrenchNav();
  const expanded: string[] = getFlows().expanded ?? [];

  function handleExpanded(_event: React.SyntheticEvent, nodeIds: string[]) {
    const nodes = new TreeViewToggle().onNodeToggle(nodeIds);
    onNav({ type: 'FLOWS', id: nodes.main, expanded: [...nodes.expanded] })
  }

  return (
    <FlowsListRoot className={classes.root}>
      <Typography className={classes.title} variant='h1'>{intl.formatMessage({ id: 'main.flows.all' })}</Typography>

      <SimpleTreeView expandedItems={expanded}
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
        onExpandedItemsChange={handleExpanded}>
        
        { Object.values(session.site.flows)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)) )
          .map(flow => (<FlowItem key={flow.id} flowId={flow.id} />))
        }
      </SimpleTreeView>
    </FlowsListRoot>
  );
}


