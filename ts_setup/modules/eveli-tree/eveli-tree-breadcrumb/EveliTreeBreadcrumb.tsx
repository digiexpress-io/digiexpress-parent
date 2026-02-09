import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors, getNodeColor } from '../tree-theme';

export const EveliTreeBreadcrumb: React.FC = () => {
  const { isDarkMode, activeTabPath, openTabs, activeTabIndex } = useEveliTree();

  if (!activeTabPath || !openTabs[activeTabIndex]) {
    return null;
  }

  const activeTab = openTabs[activeTabIndex];
  const pathParts = activeTabPath.split(' / ');
  const assetName = pathParts[pathParts.length - 1];
  const pathPrefix = pathParts.slice(0, -1).join(' / ');
  const assetColor = getNodeColor(activeTab.type, isDarkMode);

  return (
    <BreadcrumbContainer isDarkMode={isDarkMode}>
      <Box sx={{ display: 'flex' }}>
        {pathPrefix && (
          <Typography variant="subtitle2" sx={{ color: isDarkMode ? TreeColors.dark.textSecondary : TreeColors.light.textSecondary }}>
            {pathPrefix} /&nbsp;
          </Typography>
        )}
        <Typography variant="subtitle2" sx={{ color: assetColor, fontWeight: 500 }}>
          {assetName}
        </Typography>
      </Box>
    </BreadcrumbContainer>
  );
};

const BreadcrumbContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  height: 30,
  display: 'flex',
  alignItems: 'center',
  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  width: '100%',
  borderBottom: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`
}));