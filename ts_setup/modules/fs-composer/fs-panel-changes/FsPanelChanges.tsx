import React from 'react';
import { Typography, IconButton, Tooltip } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useIntl } from 'react-intl';

import { FsPanel } from '../fs-panel';
import { FsDirentButtonSaveAll } from '../fs-dirent-button-save';
import { FsDirentButtonCancelAll } from '../fs-dirent-button-cancel';
import { FsPanelChangesProps } from './FsPanelChangesProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelChangesRoot, useUtilityClasses } from './useUtilityClasses';
import { UndoConfirmDialog } from './FsChangesConfirmDialog';


export const FsPanelChanges: React.FC<FsPanelChangesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const { confirmOpen, setConfirmOpen, changes, onSave, onSaveAll, onDiscard, onDiscardAll } = ownerState;
  const classes = useUtilityClasses();

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.changesView.title' })} icon={<FsIcon icon={FsIcons.Save} large />} activeDirent={true}>

      <FsPanelChangesRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.actionBar}>
          <FsDirentButtonCancelAll onClick={() => setConfirmOpen(true)} disabled={!changes.length} />
          <FsDirentButtonSaveAll onClick={onSaveAll} disabled={!changes.length} />
      </div>
      {confirmOpen && (
        <UndoConfirmDialog
          open={confirmOpen}
          onClose={() => setConfirmOpen(false)}
          onConfirm={onDiscardAll}
        />
        )}
        <>
        {changes.map((asset) => (
          <div key={asset.id} className={classes.changeRow}>
            <div className={classes.assetName}>
              <Typography className={classes.assetTitle}>{asset.name}</Typography>
              <Typography className={classes.assetPath}>{asset.fullPath}</Typography>
            </div>

            <Tooltip title={intl.formatMessage({ id: 'fs.changesView.undo' })}>
              <IconButton size="small" onClick={() => onDiscard(asset.id)} className={classes.undoButton}>
                <FsIcon icon={FsIcons.Undo} medium />
              </IconButton>
            </Tooltip>

            <Tooltip title={intl.formatMessage({ id: 'fs.changesView.save' })}>
              <IconButton size="small" onClick={() => onSave(asset.id)} className={classes.undoButton}>
                <FsIcon icon={FsIcons.Save} medium />
              </IconButton>
            </Tooltip>
          </div>
        ))}
        </>
      </FsPanelChangesRoot>
    </FsPanel>
  );
};
