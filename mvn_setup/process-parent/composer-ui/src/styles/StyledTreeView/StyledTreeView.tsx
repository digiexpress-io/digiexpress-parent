import React from 'react';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import DeClient from '@declient';
import TreeViewToggle from './TreeViewToggle';



const StyledTreeView: React.FC<{children: React.ReactNode}> = () => {
  const { session } = DeClient.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());
  const revisions = Object.values(session.head.projects);

  if (!revisions) {
    console.log("Service explorer:: no revisions in site");
    return null;
  }

  return (
    <TreeView expanded={toggle.expanded}
      defaultCollapseIcon={<ArrowDropDownIcon />}
      defaultExpandIcon={<ArrowRightIcon />}
      defaultEndIcon={<div style={{ width: 24 }} />}
      onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
    </TreeView>
  );
}

export { StyledTreeView };

