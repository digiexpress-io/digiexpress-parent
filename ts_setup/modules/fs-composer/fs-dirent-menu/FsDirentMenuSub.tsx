import React from 'react';
import { Box, Collapse, Divider } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';
import { FsDirentPermissions } from '../fs-dirent-permissions';
import { FsDirentHistory } from '../fs-dirent-history';
import { FsDirentReferences } from '../fs-dirent-references';
import { FsDirentCreate } from '../fs-dirent-create';
import { FsDirentComments } from '../fs-dirent-comments';
import { FsDirentLabels } from '../fs-dirent-labels';

export interface FsDirentMenuSubProps {
  node: FsNode | undefined;
  openSubmenu: string | undefined;
}

export const FsDirentMenuSub: React.FC<FsDirentMenuSubProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <Collapse orientation="horizontal" in={!!props.openSubmenu}>
      <Divider orientation="vertical" className={classes.dividerSub} />
      <Box className={classes.sectionSub}>
        {props.openSubmenu === 'labels' && <FsDirentLabels node={props.node} />}
        {props.openSubmenu === 'comments' && <FsDirentComments node={props.node} />}
        {props.openSubmenu === 'sharing' && <FsDirentPermissions node={props.node} />}
        {props.openSubmenu === 'history' && <FsDirentHistory node={props.node} />}
        {props.openSubmenu === 'references' && <FsDirentReferences node={props.node} />}
        {props.openSubmenu === 'new' && <FsDirentCreate />}
      </Box>
    </Collapse>
  );
};