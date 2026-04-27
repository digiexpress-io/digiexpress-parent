import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesPrintoutProps {
  dirent: Fs.DirentBase;
  children: Fs.DirentBase[];
}

export const FsPropertiesPrintout: React.FC<FsPropertiesPrintoutProps> = ({ dirent, children }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'PRINTOUT') {
    return undefined;
  }

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutServiceName' })}</Typography>
        <Typography className={classes.propertyValue}>{(dirent.props as Fs.PrintoutProps)?.printoutServiceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.orchestratorName' })}</Typography>
        <Typography className={classes.propertyValue}>{(dirent.props as Fs.PrintoutProps)?.orchestratorName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
        <div className={classes.commentList}>
          {children.map((child) => (
            <div key={child.id} className={classes.childRow}>
              <FsIcon small icon={FsIcons.Pdf} />
              <Typography className={classes.propertyValue}>{child.name}</Typography>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};
