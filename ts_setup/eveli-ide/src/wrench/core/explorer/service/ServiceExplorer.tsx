import React from 'react';
import { Box } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";

import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";



import { Composer } from '../../context';
import ServiceItem from './ServiceItem';
import TreeViewToggle from '../TreeViewToggle';

const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}

const ServiceExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());

  return (
    <Box>
      <SimpleTreeView expandedItems={toggle.expanded}
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
        onExpandedItemsChange={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
        { Object.values(session.site.services)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)) )
          .map(service => (<ServiceItem key={service.id} serviceId={service.id} />))
        }
      </SimpleTreeView>
    </Box>
  );
}

export { ServiceExplorer };

