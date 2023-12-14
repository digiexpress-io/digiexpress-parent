import React from 'react';
import { Box } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import { Composer } from '../../context';
import ServiceItem from './ServiceItem';
import TreeViewToggle from '../TreeViewToggle';


const ServiceExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());

  return (
    <Box>
      <TreeView expanded={toggle.expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
        { Object.values(session.site.services)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)) )
          .map(service => (<ServiceItem key={service.id} serviceId={service.id} />))
        }
      </TreeView>
    </Box>
  );
}

export { ServiceExplorer };

