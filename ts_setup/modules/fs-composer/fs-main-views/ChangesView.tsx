import React from 'react';
import { Typography, Box, styled, IconButton, Tooltip } from '@mui/material';
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


export interface ChangesViewProps {
  node: FsNode | undefined;
}

export const ChangesView: React.FC<ChangesViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();


  const getStatusColor = (status: string) => {
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
              color: getStatusColor(asset.status),
              width: '100px',
              flexShrink: 0,
              textAlign: 'right'
            }}
          >
            {asset.status}
          </Typography>

          <Tooltip title="Undo changes">
            <IconButton size="small" sx={{
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
      {mainContent}
    </ViewContainer>
  );
};

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

