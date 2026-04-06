import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';
import { useUtilityClasses, FsDirentReferencesRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';


export const FsDirentReferences: React.FC<FsDirentReferencesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const { dirent } = props;
  const classes = useUtilityClasses();

  const references = React.useMemo(() => {
    if (!dirent) {
      return [];
    }
    return ownerState.findReferencesToDirent(dirent);
  }, [dirent, ownerState.findReferencesToDirent]);

  return (
    <FsDirentReferencesRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>
        {references.length > 0
          ? intl.formatMessage({ id: 'fs.direntReferences.title.referenced' }, { count: references.length })
          : intl.formatMessage({ id: 'fs.direntReferences.title.notReferenced' })}
      </Typography>
      {references.length > 0 && (
        <div className={classes.tableContainer}>
          <div className={classes.tableRow}>
            <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntReferences.tableHeader.asset' })}</div>
            <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntReferences.tableHeader.location' })}</div>
          </div>
          <div className={classes.divider} />
          {references.map((reference, index) => (
            <div key={index} className={classes.tableRow}>
              <div className={classes.tableCell}>{reference.assetName}</div>
              <div className={classes.tableCell}>{reference.location}</div>
            </div>
          ))}
        </div>
      )}
    </FsDirentReferencesRoot>
  );
};

