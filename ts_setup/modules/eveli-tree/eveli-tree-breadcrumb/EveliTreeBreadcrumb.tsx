import React from 'react';
import { Box, styled, Typography, IconButton, Popover, useTheme, alpha, lighten } from '@mui/material';
import { TreeNode, useEveliTree } from '../../eveli-tree-api';
import { TreeColors, TreeIcons } from '../tree-theme';

export const EveliTreeBreadcrumb: React.FC = () => {
  const { isDarkMode, activeTabPath, openTabs, activeTabIndex, activeNode } = useEveliTree();
  const [popoverOpen, setPopoverOpen] = React.useState(false);
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | undefined>();

  const theme = useTheme();
  if (!activeTabPath || !openTabs[activeTabIndex]) {
    return null;
  }

  const pathParts = activeTabPath.split(' / ');
  const assetName = pathParts[pathParts.length - 1];
  const pathPrefix = pathParts.slice(0, -1).join(' / ');


  function handleHelpClick(event: React.MouseEvent<HTMLElement>) {
    setAnchorEl(event.currentTarget);
    setPopoverOpen(true);
  }

  function handlePopoverClose() {
    setPopoverOpen(false);
    setAnchorEl(undefined);
  }

  return (
    <BreadcrumbContainer isDarkMode={isDarkMode} activeNode={activeNode}>
      <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
        {activeNode?.error && (
          <IconButton size="small" color='error' onClick={handleHelpClick}>
            <TreeIcons.Error fontSize="small" sx={{ color: isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight }} />
          </IconButton>
        )}
        <Popover
          open={popoverOpen}
          anchorEl={anchorEl}
          onClose={handlePopoverClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
          slotProps={{
            paper: {
              sx: {
                color: TreeColors.light.text,
                border: `1px solid ${alpha(isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight, 0.5)}`,
                backgroundColor: lighten(TreeColors.semantic.dangerLight, 0.95),
                borderRadius: theme.spacing(1),
                minWidth: 300,
                maxWidth: 350,
                mt: 1
              }
            }
          }}
        >
          <Box sx={{ p: 1 }}>
            <Typography variant='subtitle2' sx={{ fontWeight: 500 }}>
              An issue with this asset needs attention.
            </Typography>
            <Typography variant='subtitle2' sx={{ fontStyle: 'italic' }}>
              Page en does not have a Markdown level 1 heading.
            </Typography>
            <Box mb={1} />
            <Typography variant='subtitle2'>
              Add a level one heading (# Example heading) to render this page in the client portal.
            </Typography>
          </Box>
        </Popover>
        {pathPrefix && (
          <Typography variant="subtitle2" sx={{ color: isDarkMode ? TreeColors.dark.textSecondary : TreeColors.light.textSecondary }}>
            {pathPrefix} /&nbsp;
          </Typography>
        )}
        <Typography variant="subtitle2" sx={{ fontWeight: 500 }}>
          {assetName}
        </Typography>
      </Box>
    </BreadcrumbContainer>
  );
};

const BreadcrumbContainer = styled(Box, {
  shouldForwardProp: (prop) => !['isDarkMode', 'activeNode'].includes(prop as string)
})<{ isDarkMode: boolean, activeNode: TreeNode | undefined }>(({ isDarkMode, theme }) => ({
  height: 30,
  display: 'flex',
  alignItems: 'center',
  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  width: '100%',
  borderBottom: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  backgroundColor: 'inherit'
}));