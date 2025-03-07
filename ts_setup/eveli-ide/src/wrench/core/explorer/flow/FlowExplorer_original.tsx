import React from 'react';
import { Box } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";


import { Composer } from '../../context';
import FlowItem from './FlowItem';
import TreeViewToggle from '../TreeViewToggle';

const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}


const FlowExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());
  return (
    <Box>
      <SimpleTreeView expandedItems={toggle.expanded}
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
        onExpandedItemsChange={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
        
        { Object.values(session.site.flows)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)) )
          .map(flow => (<FlowItem key={flow.id} flowId={flow.id} />))
        }
      </SimpleTreeView>
    </Box>
  );
}

export { FlowExplorer }

