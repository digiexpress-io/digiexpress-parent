import React from 'react';
import { Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirentProps } from '@dxs-ts/fs-api';
import { FsDirentPermissionsProps } from './FsDirentPermissionsProps';
import { useUtilityClasses, FsDirentPermissionsRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentPermissions: React.FC<FsDirentPermissionsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const { getDirentProps } = useFsDirentProps();
  const direntProps = props.dirent ? getDirentProps(props.dirent.id) : undefined;

  return (
    <FsDirentPermissionsRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.description}>{intl.formatMessage({ id: 'fs.direntPermissions.message.readWrite' })}</Typography>
      <div className={classes.tableContainer}>
        <div className={classes.tableRow}>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntPermissions.tableHeader.name' })}</div>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntPermissions.tableHeader.privilege' })}</div>
        </div>
        <Divider className={classes.divider} />
        {(direntProps?.permissions ?? []).map((permission, index) => (
          <div key={index} className={classes.tableRow}>
            <div className={classes.tableCell}>{permission.name}</div>
            <div className={classes.tableCell}>
              {permission.types.map(p => intl.formatMessage({ id: `fs.direntPermissionType.${p}` })).join(', ')}
            </div>
          </div>
        ))}
      </div>
    </FsDirentPermissionsRoot>
  );
};
