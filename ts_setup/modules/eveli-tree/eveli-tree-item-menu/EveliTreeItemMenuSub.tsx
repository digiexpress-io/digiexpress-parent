import React from 'react';
import { Box, Collapse, Divider, TextField } from '@mui/material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses } from './useUtilityClasses';
import { ItemSharingAndPermissions } from './ItemSharingAndPermissions';
import { ItemHistory } from './ItemHistory';
import { ItemReferences } from './ItemReferences';
import { ItemCreate } from './ItemCreate';
import { ItemComments } from './ItemComments';

export interface EveliTreeItemMenuSubProps {
  node: TreeNode | undefined;
  openSubmenu: string | undefined;
}

export const EveliTreeItemMenuSub: React.FC<EveliTreeItemMenuSubProps> = (props) => {
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
        {props.openSubmenu === 'comments' && <ItemComments node={props.node} />}
        {props.openSubmenu === 'sharing' && <ItemSharingAndPermissions />}
        {props.openSubmenu === 'history' && <ItemHistory />}
        {props.openSubmenu === 'references' && <ItemReferences node={props.node} />}
        {props.openSubmenu === 'new' && <ItemCreate />}
      </Box>
    </Collapse>
  );
};