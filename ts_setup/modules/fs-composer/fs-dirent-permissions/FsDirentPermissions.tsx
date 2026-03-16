import React from 'react';
import { Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentPermissionsProps } from './FsDirentPermissionsProps';
import { useUtilityClasses, FsDirentPermissionsRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentPermissions: React.FC<FsDirentPermissionsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentPermissionsRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.description}>{intl.formatMessage({ id: 'fs.direntPermissions.message.readWrite' })}</Typography>
      <div className={classes.tableContainer}>
        <div className={classes.tableRow}>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntPermissions.tableHeader.name' })}</div>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntPermissions.tableHeader.privilege' })}</div>
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

interface DirentPermissions {
  name: string;
  privilege: string;
}
const permissions: DirentPermissions[] = [
  { name: 'John Smith (Me)', privilege: 'Read & write' },
  { name: 'Diana Hasselback', privilege: 'Read & write' },
  { name: 'office-staff', privilege: 'read' },
  { name: 'part-time staff', privilege: 'read' },
  { name: 'everyone', privilege: 'read' }
];


