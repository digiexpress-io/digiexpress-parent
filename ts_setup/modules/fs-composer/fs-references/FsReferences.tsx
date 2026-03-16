import React from 'react';
import { Box, Typography } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { FsPanel } from '../fs-panel';
import { FsReferencesProps } from './FsReferencesProps';
import { useOwnerState } from './useOwnerState';
import { FsReferencesRoot, useUtilityClasses } from './useUtilityClasses';


export const FsReferences: React.FC<FsReferencesProps> = (props) => {
  const ownerState = useOwnerState(props);
  const { node } = props;
  const { findReferencesToNode } = useFs();
  const classes = useUtilityClasses();

  if (!node) {
    return (
      <FsPanel title="References" icon={<FsIcon icon={FsIcons.Tree} large />} activeNode={false} noNodeMessage="Select a node from the tree to view references.">
        <></>
      </FsPanel>
    );
  }

  const references = findReferencesToNode(node);

  const secondaryContent = node.children && node.children.length > 0 ? (
    <div className={classes.childrenSection}>
      <Typography variant="subtitle2">Child References</Typography>
      {node.children.map((child) => (
        <Box key={child.id}>
          <Typography variant="body2">{child.name}</Typography>
          <Typography variant="caption" color="text.secondary">
            Type: {child.type} {child.reference && ' (REF)'}
          </Typography>
        </Box>
      ))}
    </div>
  ) : undefined;

  return (
    <FsPanel title={`References: ${node.name}`} icon={<FsIcon icon={FsIcons.Tree} large />} secondaryChildren={secondaryContent} activeNode={true}>
      <FsReferencesRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.referenceSection}>
          {references.length > 0 ? (
            <div className={classes.referencesContainer}>
              {references.map((ref, index) => (
                <div key={index} className={classes.referenceRow}>
                  <Typography className={classes.referenceLocation}>{ref.location}</Typography>
                </div>
              ))}
            </div>
          ) : (
            <Typography variant="body2" color="text.secondary">
              This node does not contain any references.
            </Typography>
          )}
        </div>
      </FsReferencesRoot>
    </FsPanel>
  );
};


