import React from 'react';
import { Box, Divider } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';
import { FsDirentMenuNew } from '../fs-dirent-menu-new';
import { FsDirentRename } from '../fs-dirent-rename';
import { FsDirentLabels } from '../fs-dirent-labels';
import { FsDirentDescription } from '../fs-dirent-description';


export interface FsDirentMenuSubProps {
  dirent: Fs.DirentBase | undefined;
  openSubmenu: string | undefined;
  onClose: () => void;
}

export const FsDirentMenuSub: React.FC<FsDirentMenuSubProps> = React.memo((props) => {
  const classes = useUtilityClasses();

  if (!props.openSubmenu) {
    return null;
  }

  return (
    <>
      <Divider orientation="vertical" className={classes.dividerSub} />
      <Box className={classes.sectionSub}>
        {props.openSubmenu === 'rename' && <FsDirentRename dirent={props.dirent} />}
        {props.openSubmenu === 'new' && <FsDirentMenuNew dirent={props.dirent} onClose={props.onClose} />}
        {props.openSubmenu === 'description' && props.dirent && <FsDirentDescription dirent={props.dirent} />}
        {props.openSubmenu === 'labels' && props.dirent && <FsDirentLabels dirent={props.dirent} />}
      </Box>
    </>
  );
});