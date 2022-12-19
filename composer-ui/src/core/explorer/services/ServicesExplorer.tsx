import React from 'react';
import { Box } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import DeClient from '@declient';

import RevisionsValue from './RevisionsValue';
import HeadValue from './HeadValue';
import ReleasesValue from './ReleasesValue';
import TreeViewToggle from '../TreeViewToggle';



const ServicesExplorer: React.FC<{}> = () => {
  const { session } = DeClient.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());
  const revisions = Object.values(session.head.projects);
  
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

        <HeadValue value={session.head} />
        <RevisionsValue value={session.head.projects} />
        <ReleasesValue value={session.head.releases} />

      </TreeView>
    </Box>
  );
}

export { ServicesExplorer };

