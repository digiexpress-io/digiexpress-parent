import React from 'react';
import { Box, Typography, styled, List, ListItem, ListItemText, Chip } from '@mui/material';
import { TreeNode } from '../../../eveli-tree-api';
import { TreeColors, TreeIcons } from '../../tree-theme';
import { useEveliTree } from '../../../eveli-tree-api';

interface ReferencesViewProps {
  node: TreeNode;
}

export const ReferencesView: React.FC<ReferencesViewProps> = ({ node }) => {
  const { isDarkMode } = useEveliTree();

  // Check if this node has references
  const hasReferences = node.reference;

  return (
    <ViewContainer isDarkMode={isDarkMode}>
      <Header>
        <TreeIcons.Tree sx={{ mr: 1, fontSize: '1.5rem' }} />
        <Typography variant="h6">References</Typography>
      </Header>

      <Content>
        <NodeInfo>
          <Typography variant="subtitle1" gutterBottom>
            {node.name}
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Type: {node.type} • ID: {node.id}
          </Typography>

          {hasReferences ? (
            <Chip
              label="Has References"
              color="primary"
              size="small"
              icon={<TreeIcons.Tree />}
              sx={{ mt: 1 }}
            />
          ) : (
            <Chip
              label="No References"
              color="default"
              size="small"
              sx={{ mt: 1 }}
            />
          )}
        </NodeInfo>

        <ReferenceSection>
          <Typography variant="subtitle2" gutterBottom sx={{ mt: 3, mb: 2 }}>
            Reference Information
          </Typography>

          {hasReferences ? (
            <Box>
              <Typography variant="body2" sx={{ mb: 2 }}>
                This node is marked as a reference and may be used by other components in the tree structure.
              </Typography>

              <ReferenceDetails isDarkMode={isDarkMode}>
                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                  reference: {String(node.reference)}
                </Typography>
                {node.description && (
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    Description: {node.description}
                  </Typography>
                )}
              </ReferenceDetails>
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary">
              This node does not contain any reference markers.
            </Typography>
          )}
        </ReferenceSection>

        {node.children && node.children.length > 0 && (
          <ChildrenSection>
            <Typography variant="subtitle2" gutterBottom sx={{ mt: 3, mb: 2 }}>
              Child References
            </Typography>
            <List dense>
              {node.children.map((child) => (
                <ListItem key={child.id} sx={{ py: 0.5 }}>
                  <ListItemText
                    primary={child.name}
                    secondary={
                      <Box component="span" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <span>Type: {child.type}</span>
                        {child.reference && (
                          <Chip
                            label="REF"
                            size="small"
                            color="primary"
                            sx={{ height: '16px', fontSize: '10px' }}
                          />
                        )}
                      </Box>
                    }
                  />
                </ListItem>
              ))}
            </List>
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
  padding: '16px',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
  overflow: 'auto'
}));

const Header = styled(Box)(() => ({
  display: 'flex',
  alignItems: 'center',
  marginBottom: '20px',
  paddingBottom: '12px',
  borderBottom: '2px solid',
  borderColor: 'rgba(255, 255, 255, 0.1)'
}));

const Content = styled(Box)(() => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '16px'
}));

const NodeInfo = styled(Box)(() => ({
  padding: '12px',
  borderRadius: '8px',
  backgroundColor: 'rgba(255, 255, 255, 0.05)',
  border: '1px solid rgba(255, 255, 255, 0.1)'
}));

const ReferenceSection = styled(Box)(() => ({
  // No specific styling needed
}));

const ReferenceDetails = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  padding: '12px',
  borderRadius: '6px',
  backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  border: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  marginTop: '8px'
}));

const ChildrenSection = styled(Box)(() => ({
  // No specific styling needed
}));