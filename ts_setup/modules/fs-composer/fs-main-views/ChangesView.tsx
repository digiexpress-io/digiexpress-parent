import React from 'react';
import { Typography, styled, IconButton, Tooltip, Dialog, DialogActions, Button, DialogTitle, DialogContent, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';


const assetsWithChanges = [
  { id: 'main.article', name: 'main.article', status: 'modified' },
  { id: 'info-gdpr.article', name: 'info-gdpr.article', status: 'modified' },
  { id: 'general-message.service', name: 'general-message.service', status: 'modified' },
  { id: 'taskMsgFlow.flow', name: 'taskMsgFlow.flow', status: 'new' },
  { id: 'public-inforeq.service', name: 'public-inforeq.service', status: 'deleted' },
  { id: 'trustee-info-form.service', name: 'trustee-info-form.service', status: 'modified' },
  { id: 'sipoo-main-site.link', name: 'sipoo-main-site.link', status: 'new' }
];

const getStatusColor = (status: string, isDarkMode: boolean) => {
  switch (status) {
    case 'deleted':
      return isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight;
    case 'new':
      return FsColors.semantic.success;
    case 'modified':
      return isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight;
    default:
      return isDarkMode ? FsColors.dark.text : FsColors.light.text;
  }
};

export interface ChangesViewProps {
  node: FsNode | undefined;
}

export const ChangesView: React.FC<ChangesViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const classes = useUtilityClasses(isDarkMode);


  return (
    <ViewContainer title='Unsaved changes' icon={<FsIcons.Save />} activeNode={true}>
      <div className={classes.actionBar}>
        <Button variant='outlined' className={classes.discardButton}>Discard all changes</Button>
        <Button>Save all changes</Button>
      </div>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      <ChangesViewRoot className={classes.root} isDarkMode={isDarkMode}>
        {assetsWithChanges.map((asset) => (
          <div key={asset.id} className={classes.changeRow}>
            <Typography variant="subtitle2" className={classes.assetName}>
              {asset.name}
            </Typography>
            <Typography
              variant="subtitle2"
              className={classes.statusText}
              style={{ color: getStatusColor(asset.status, isDarkMode) }}
            >
              {asset.status}
            </Typography>

            <Tooltip title="Undo changes">
              <IconButton
                size="small"
                onClick={() => setConfirmOpen(true)}
                className={classes.undoButton}
              >
                <FsIcons.Undo />
              </IconButton>
            </Tooltip>
          </div>
        ))}
      </ChangesViewRoot>
    </ViewContainer>
  );
};


const UndoConfirmDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>Undo changes to asset</DialogTitle>
      <DialogContent>
        You are about to discard all changes to this file. This action cannot be undone.
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>Cancel</Button>
        <Button onClick={onClose}>Accept</Button>
      </DialogActions>
    </Dialog>
  )
}

const MUI_NAME = 'ChangesView';

export interface ChangesViewClasses {
  root: string;
  changeRow: string;
  assetName: string;
  statusText: string;
  undoButton: string;
  actionBar: string;
  discardButton: string;
}

export type ChangesViewClassKey = keyof ChangesViewClasses;

const useUtilityClasses = (isDarkMode: boolean) => {
  const slots = {
    root: ['root'],
    changeRow: ['changeRow'],
    assetName: ['assetName'],
    statusText: ['statusText'],
    undoButton: ['undoButton'],
    actionBar: ['actionBar'],
    discardButton: ['discardButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const ChangesViewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

  [`& .${MUI_NAME}-changeRow:nth-of-type(odd)`]: {
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-changeRow`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(2),
    padding: theme.spacing(1, 1.5),
    backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '&:last-child': {
      borderBottom: 'none'
    }
  },

  [`& .${MUI_NAME}-assetName`]: {
    fontWeight: 500,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    width: '300px',
    flexShrink: 0
  },

  [`& .${MUI_NAME}-statusText`]: {
    fontWeight: 500,
    width: '100px',
    flexShrink: 0,
    textAlign: 'right'
  },

  [`& .${MUI_NAME}-undoButton`]: {
    marginLeft: 'auto',
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&:hover': {
      backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface
    }
  },

  [`& .${MUI_NAME}-actionBar`]: {
    display: 'flex',
    justifyContent: 'center',
    gap: theme.spacing(1),
    marginBottom: theme.spacing(1)
  },

  [`& .${MUI_NAME}-discardButton`]: {
    color: isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
  },
}));

