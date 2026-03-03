import React from 'react';
import { Typography } from '@mui/material';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';
import { useUtilityClasses, FsDirentReferencesRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';


export const FsDirentReferences: React.FC<FsDirentReferencesProps> = (props) => {
  const ownerState = useOwnerState(props);
  const { node } = props;
  const classes = useUtilityClasses();

  const references = React.useMemo(() => {
    if (!node) {
      return [];
    }
    return ownerState.findReferencesToNode(node);
  }, [node, ownerState.findReferencesToNode]);

  return (
    <FsDirentReferencesRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>
        {references.length > 0 ? `This asset is referenced in ${references.length} location(s)` : 'This asset is not referenced anywhere'}
      </Typography>
      {references.length > 0 && (
        <div className={classes.tableContainer}>
          <div className={classes.tableRow}>
            <div className={classes.tableHeader}>Asset</div>
            <div className={classes.tableHeader}>Location</div>
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

