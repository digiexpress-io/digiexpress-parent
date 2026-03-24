import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { useFsDirentProps } from '@dxs-ts/fs-api';
import { FsPanel } from '../fs-panel';
import { FsReferencesProps } from './FsReferencesProps';
import { useOwnerState } from './useOwnerState';
import { FsReferencesRoot, useUtilityClasses } from './useUtilityClasses';


export const FsReferences: React.FC<FsReferencesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const { dirent } = props;
  const { findReferencesToDirent, getDirent } = useFsDirentProps();
  const classes = useUtilityClasses();

  if (!dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.references.title' })}
        icon={<FsIcon icon={FsIcons.Tree} large />}
        activeDirent={false}
        noDirentMessage={intl.formatMessage({ id: 'fs.references.message.selectDirent' })} />
    );
  }

  const references = findReferencesToDirent(dirent);

  const secondaryContent = dirent.children && dirent.children.length > 0 ? (
    <div className={classes.childrenSection}>
      <Typography variant="subtitle2">{intl.formatMessage({ id: 'fs.references.sectionTitle.childReferences' })}</Typography>
      {dirent.children.map((child) => {
        const childProps = getDirent(child.id);
        return (
          <Box key={child.id}>
            <Typography variant="body2">{child.name}</Typography>
            <Typography variant="caption">
              {intl.formatMessage({ id: 'fs.references.label.type' }, { childType: child.type })}{childProps?.reference && ` ${intl.formatMessage({ id: 'fs.references.label.refMarker' })}`}
            </Typography>
          </Box>
        );
      })}
    </div>
  ) : undefined;

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.references.title.direntName' }, { direntName: dirent.name })} icon={<FsIcon icon={FsIcons.Tree} large />} secondaryChildren={secondaryContent} activeDirent={true}>
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
              <Typography variant="body2">
                {intl.formatMessage({ id: 'fs.references.message.noReferences' })}
            </Typography>
          )}
        </div>
      </FsReferencesRoot>
    </FsPanel>
  );
};


