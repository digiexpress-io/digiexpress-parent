import React from 'react';
import { Box, Collapse, Divider, TextField } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';
import { FsDirentPermissions } from '../fs-dirent-permissions';
import { FsDirentHistory } from '../fs-dirent-history';
import { FsDirentReferences } from '../fs-dirent-references';
import { FsDirentCreate } from '../fs-dirent-create';
import { FsDirentComments } from '../fs-dirent-comments';

export interface FsDirentMenuSubProps {
  node: FsNode | undefined;
  openSubmenu: string | undefined;
}

export const FsDirentMenuSub: React.FC<FsDirentMenuSubProps> = (props) => {
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
        {props.openSubmenu === 'comments' && <FsDirentComments node={props.node} />}
        {props.openSubmenu === 'sharing' && <FsDirentPermissions />}
        {props.openSubmenu === 'history' && <FsDirentHistory />}
        {props.openSubmenu === 'references' && <FsDirentReferences node={props.node} />}
        {props.openSubmenu === 'new' && <FsDirentCreate />}
      </Box>
    </Collapse>
  );
};