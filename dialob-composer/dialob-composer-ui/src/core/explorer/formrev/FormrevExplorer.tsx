import React from 'react';
import { Box } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import { Composer } from '../../context';
import FormrevItem from './FormrevItem';
import TreeViewToggle from '../TreeViewToggle';


const FormrevExplorer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [toggle, setToggle] = React.useState(new TreeViewToggle());
  return (
    <Box>
      <TreeView expanded={toggle.expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => setToggle(toggle.onNodeToggle(nodeIds))}>
        
        { Object.values(session.site.revs)
          .sort((a, b) => (a.name ? a.name : a.id).localeCompare((b.name ? b.name : b.id)) )
          .map(flow => (<FormrevItem key={flow.id} formrevId={flow.id} />))
        }
      </TreeView>
    </Box>
  );
}

export { FormrevExplorer }

