import React from 'react';
import { Typography, Box, styled, IconButton, Tooltip, Dialog, DialogActions, Button, DialogTitle, DialogContent } from '@mui/material';
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




  const mainContent = (
    <ChangesContainer isDarkMode={isDarkMode}>
      {assetsWithChanges.map((asset) => (
        <ChangeRow key={asset.id} isDarkMode={isDarkMode}>
          <Typography variant="subtitle2"
            sx={{
              fontWeight: 500,
              color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
              width: '300px',
              flexShrink: 0
            }}
          >
            {asset.name}
          </Typography>
          <Typography variant="subtitle2"
            sx={{
              fontWeight: 500,
              color: getStatusColor(asset.status, isDarkMode),
              width: '100px',
              flexShrink: 0,
              textAlign: 'right'
            }}
          >
            {asset.status}
          </Typography>

          <Tooltip title="Undo changes">
            <IconButton size="small" onClick={() => setConfirmOpen(true)}
              sx={{
                marginLeft: 'auto',
                color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
                '&:hover': {
                  backgroundColor: isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)'
                }
              }}
            >
              <FsIcons.Undo />
            </IconButton>
          </Tooltip>
        </ChangeRow>
      ))}
    </ChangesContainer>
  );

  return (
    <ViewContainer title='Unsaved changes' icon={<FsIcons.Save />} activeNode={true}>
      <Box display='flex' justifyContent='center' gap={1} mb={1}>
        <Button variant='outlined' color='error' sx={{ color: 'error.main' }}>Discard all changes</Button>
        <Button>Save all changes</Button>
      </Box>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      {mainContent}
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

const ChangesContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  '& > div:nth-of-type(odd)': {
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },
}));

const ChangeRow = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode?: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '16px',
  padding: '8px 12px',
  backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
  borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  '&:last-child': {
    borderBottom: 'none'
  }
}));

