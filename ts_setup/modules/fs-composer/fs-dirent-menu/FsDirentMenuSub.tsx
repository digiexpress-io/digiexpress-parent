import React from 'react';
import { Box, Collapse, Divider } from '@mui/material';
import { FsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';
import { FsDirentPermissions } from '../fs-dirent-permissions';
import { FsDirentHistory } from '../fs-dirent-history';
import { FsDirentReferences } from '../fs-dirent-references';
import { FsDirentCreate } from '../fs-dirent-create';
import { FsDirentComments } from '../fs-dirent-comments';
import { FsDirentLabels } from '../fs-dirent-labels';

export interface FsDirentMenuSubProps {
  dirent: FsDirent | undefined;
  openSubmenu: string | undefined;
}

export const FsDirentMenuSub: React.FC<FsDirentMenuSubProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <Collapse orientation="horizontal" in={!!props.openSubmenu}>
      <Divider orientation="vertical" className={classes.dividerSub} />
      <Box className={classes.sectionSub}>
        {props.openSubmenu === 'labels' && <FsDirentLabels dirent={props.dirent} />}
        {props.openSubmenu === 'comments' && <FsDirentComments dirent={props.dirent} />}
        {props.openSubmenu === 'sharing' && <FsDirentPermissions dirent={props.dirent} />}
        {props.openSubmenu === 'history' && <FsDirentHistory dirent={props.dirent} />}
        {props.openSubmenu === 'references' && <FsDirentReferences dirent={props.dirent} />}
        {props.openSubmenu === 'new' && <FsDirentCreate />}
      </Box>
    </Collapse>
  );
};