import React from 'react';
import { Box, Collapse, Divider, TextField } from '@mui/material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses } from './useUtilityClasses';
import { EveliTreeItemSharingPermissions } from './EveliTreeItemSharingPermissions';
import { EveliTreeItemHistory } from './EveliTreeItemHistory';
import { EveliTreeItemReferences } from './EveliTreeItemReferences';
import { NewItem } from './NewItem';

export interface EveliTreeItemMenuSubProps {
  node: TreeNode | undefined;
  openSubmenu: string | undefined;
}

export const EveliTreeItemMenuSub: React.FC<EveliTreeItemMenuSubProps> = (props) => {
  const classes = useUtilityClasses();
  const [labels, setLabels] = React.useState('');
  const [comments, setComments] = React.useState('');

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
        {props.openSubmenu === 'comments' && (
          <TextField className={classes.textField} multiline minRows={2} maxRows={5}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder='Add comments...'
            size='small'
          />
        )}
        {props.openSubmenu === 'sharing' && <EveliTreeItemSharingPermissions />}
        {props.openSubmenu === 'history' && <EveliTreeItemHistory />}
        {props.openSubmenu === 'references' && <EveliTreeItemReferences node={props.node} />}
        {props.openSubmenu === 'new' && <NewItem />}
      </Box>
    </Collapse>
  );
};