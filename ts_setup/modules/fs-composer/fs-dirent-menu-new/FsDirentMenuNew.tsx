import React from 'react';
import { MenuItem, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentMenuNewRoot } from './useUtilityClasses';
import { createWidget } from '../fs-factory';


export const FsDirentMenuNew: React.FC<FsDirentMenuNewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const { openCreateTab } = useFsNav();
  const { creatableTypes } = useFsDirent();

  function handleTypeClick(type: Fs.BodyType) {
    openCreateTab(type, props.dirent);
    props.onClose();
  }
  if (!props.dirent) {
    return;
  }

  return (
    <FsDirentMenuNewRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntNew.title' })}</Typography>
      {creatableTypes.map((type) => {
        const widget = createWidget({ type });
        return (
          <MenuItem key={type} className={classes.listItem} onClick={() => handleTypeClick(type)} disableRipple>
            <widget.icons.dirent.Marker small />
            {intl.formatMessage({ id: `fs.direntNew.type.${type.toLocaleLowerCase()}` })}
          </MenuItem>
        )
      })}
    </FsDirentMenuNewRoot>
  );
};
