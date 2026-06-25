import React from 'react';
import { MenuItem, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useUtilityClasses, FsDirentMenuNewRoot } from './useUtilityClasses';
import { allWidgets } from '../fs-factory';


export const FsDirentMenuNew: React.FC<FsDirentMenuNewProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { openCreateTab } = useFsNav();

  function handleTypeClick(type: Fs.BodyType) {
    openCreateTab(type, props.dirent);
    props.onClose();
  }
  if (!props.dirent) {
    return;
  }

  return (
    <FsDirentMenuNewRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntNew.title' })}</Typography>
      {allWidgets.filter(w => w.meta.type !== 'FOLDER').map((widget) => {
        return (
          <MenuItem key={widget.meta.type} className={classes.listItem} onClick={() => handleTypeClick(widget.meta.type)} disableRipple>
            <widget.icons.dirent.Marker small />
            {intl.formatMessage({ id: `fs.dirent.type.${widget.meta.type.toLocaleLowerCase()}` })}
          </MenuItem>
        )
      })}
    </FsDirentMenuNewRoot>
  );
};
