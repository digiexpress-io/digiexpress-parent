import React from 'react';
import { Typography, Divider } from '@mui/material';
import { FsDirentPermissionsProps, permissions } from './FsDirentPermissionsProps';
import { useUtilityClasses, FsDirentPermissionsRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentPermissions: React.FC<FsDirentPermissionsProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentPermissionsRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.description}>You can read and write</Typography>
      <div className={classes.tableContainer}>
        <div className={classes.tableRow}>
          <div className={classes.tableHeader}>Name</div>
          <div className={classes.tableHeader}>Privilege</div>
        </div>
        <Divider className={classes.divider} />
        {permissions.map((permission, index) => (
          <div key={index} className={classes.tableRow}>
            <div className={classes.tableCell}>{permission.name}</div>
            <div className={classes.tableCell}>{permission.privilege}</div>
          </div>
        ))}
      </div>
    </FsDirentPermissionsRoot>
  );
};