import React from 'react';
import { Box } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import Client from '../../client';
import { Composer } from '../../context';
import { ProcessValue } from './ServicesItem';
import TreeViewToggle from '../TreeViewToggle';





const ServicesExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());
  const revision = Object.values(session.site.revisions).find(() => true);
  if(!revision) {
    console.log("Service explorer:: no revisions in site");
    return null;
  }
  const head = session.site.definitions[revision.head];
  if(!head) {
    console.log("Service explorer:: no head in site");
    return null;
  }
  
  return (
    <Box>
      <TreeView expanded={toggle.expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
        
        { head.processes
          .sort((a, b) => (a.name).localeCompare((b.name)) )
          .map(process => (<ProcessValue key={process.id} value={process} />))
        }
      </TreeView>
    </Box>
  );
}

export { ServicesExplorer };

