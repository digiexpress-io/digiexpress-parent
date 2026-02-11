import React from 'react';
import { Box, Typography, styled } from '@mui/material';
import { TreeNode } from '../../../eveli-tree-api';
import { TreeColors, TreeIcons } from '../../tree-theme';
import { useEveliTree } from '../../../eveli-tree-api';

interface ReferencesViewProps {
  node: TreeNode;
}

export const ReferencesView: React.FC<ReferencesViewProps> = ({ node }) => {
  const { isDarkMode } = useEveliTree();
  const { findReferencesToNode } = useEveliTree();
  const references = findReferencesToNode(node)

  return (
    <ViewContainer isDarkMode={isDarkMode}>


      <Content>
        <Header>
          <TreeIcons.Tree sx={{ mr: 1 }} />
          <Typography variant="body1" fontWeight={500}>References: {node.name}</Typography>
        </Header>

        <ReferenceSection>
          {references.length > 0 ? (
            <ReferencesContainer isDarkMode={isDarkMode}>
              {references.map((ref, index) => (
                <ReferenceRow key={index} isDarkMode={isDarkMode}>
                  <ReferenceLocation isDarkMode={isDarkMode}>{ref.location}</ReferenceLocation>
                  {/*<ReferenceAssetName isDarkMode={isDarkMode}>{ref.assetName}</ReferenceAssetName> */}
                </ReferenceRow>
              ))}
            </ReferencesContainer>
          ) : (
            <Typography variant="body2" color="text.secondary">
                This node does not contain any references.
            </Typography>
          )}
        </ReferenceSection>

        {node.children && node.children.length > 0 && (
          <ChildrenSection>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              Child References
            </Typography>
            {node.children.map((child) => (
              <Box key={child.id} sx={{ mb: 1 }}>
                <Typography variant="body2">{child.name}</Typography>
                <Typography variant="caption" color="text.secondary">
                  Type: {child.type} {child.reference && ' (REF)'}
                </Typography>
              </Box>
            ))}
          </ChildrenSection>
        )}
      </Content>
    </ViewContainer>
  );
};

const ViewContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
  overflow: 'auto'
}));

const Header = styled(Box)(() => ({
  display: 'flex',
  alignItems: 'center',
  marginBottom: '10px'
}));

const Content = styled(Box)(() => ({
  display: 'flex',
  flexDirection: 'column',
  padding: '8px'
}));


const ReferenceSection = styled(Box)(() => ({
  marginBottom: '16px'
}));

const ReferencesContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  '& > div:nth-of-type(odd)': {
    backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  },
}));

const ReferenceRow = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  padding: '8px 12px',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  borderBottom: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  '&:last-child': {
    borderBottom: 'none'
  }
}));

const ReferenceLocation = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  ...theme.typography.subtitle2,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
}));

const ReferenceAssetName = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  fontSize: '14px',
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
  marginTop: '2px'
}));

const ChildrenSection = styled(Box)(() => ({
  marginTop: '16px'
}));