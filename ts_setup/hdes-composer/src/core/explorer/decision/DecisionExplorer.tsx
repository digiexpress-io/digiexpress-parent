import React from 'react';
import { Box } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import { Composer } from '../../context';
import DecisionItem from './DecisionItem';
import TreeViewToggle from '../TreeViewToggle';


const DecisionExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());

  return (
    <Box>
      <TreeView expanded={toggle.expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
        { Object.values(session.site.decisions)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)) )
          .map(decision => (<DecisionItem key={decision.id} decisionId={decision.id} />))
        }
      </TreeView>
    </Box>
  );
}

export { DecisionExplorer }

