import React from 'react';
import { Box, Collapse, Divider, TextField } from '@mui/material';
import { TreeNode } from '@dxs-ts/eveli-tree-api';
import { useUtilityClasses } from './useUtilityClasses';
import { NodeSharingAndPermissions } from './NodeSharingAndPermissions';
import { NodeHistory } from './NodeHistory';
import { NodeReferences } from './NodeReferences';
import { NodeCreate } from './NodeCreate';
import { NodeComments } from './NodeComments';

export interface EveliTreeNodeMenuSubProps {
  node: TreeNode | undefined;
  openSubmenu: string | undefined;
}

export const EveliTreeNodeMenuSub: React.FC<EveliTreeNodeMenuSubProps> = (props) => {
  const classes = useUtilityClasses();
  const [labels, setLabels] = React.useState('');

  return (
    <Collapse orientation="horizontal" in={!!props.openSubmenu}>
      <Divider orientation="vertical" className={classes.dividerSub} />
      <Box className={classes.sectionSub}>
        {props.openSubmenu === 'labels' && (
          <TextField className={classes.textField} multiline minRows={2} maxRows={5}
            value={labels}
            onChange={(e) => setLabels(e.target.value)}
            placeholder='Add labels...'
            size='small'
          />
        )}
        {props.openSubmenu === 'comments' && <NodeComments node={props.node} />}
        {props.openSubmenu === 'sharing' && <NodeSharingAndPermissions />}
        {props.openSubmenu === 'history' && <NodeHistory />}
        {props.openSubmenu === 'references' && <NodeReferences node={props.node} />}
        {props.openSubmenu === 'new' && <NodeCreate />}
      </Box>
    </Collapse>
  );
};