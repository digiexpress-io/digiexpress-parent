import React from 'react';
import { Box } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import { Composer } from '../../context';
import RevisionsValue from './RevisionsValue';
import ConfigsValue from './ConfigsValue';
import HeadValue from './HeadValue';
import ReleasesValue from './ReleasesValue';
import TreeViewToggle from '../TreeViewToggle';



const ServicesExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());
  const revisions = Object.values(session.site.revisions);
  
  if (!revisions) {
    console.log("Service explorer:: no revisions in site");
    return null;
  }

  return (
    <Box>
      <TreeView expanded={toggle.expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>

        <HeadValue value={session.site} />
        <RevisionsValue value={session.site.revisions} />
        <ReleasesValue value={session.site.releases} />
        <ConfigsValue value={session.site.configs} />
                        
      </TreeView>
    </Box>
  );
}

export { ServicesExplorer };

