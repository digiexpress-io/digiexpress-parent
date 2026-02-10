import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { TreeNode, mockTreeData } from '../../eveli-tree-api';

interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

function findReferencesToNode(nodeId: string, nodeName: string): ItemReferencesEntry[] {
  const references: ItemReferencesEntry[] = [];

  function searchInNode(node: TreeNode, path: string[] = []): void {
    const currentPath = [...path, node.name];

    // Check if this node is a reference to our target node
    if (node.reference && node.name === nodeName && node.id !== nodeId) {
      references.push({
        assetName: node.name,
        location: currentPath.slice(0, -1).join(' / ')
      });
    }

    // Recursively search children
    if (node.children) {
      node.children.forEach(child => searchInNode(child, currentPath));
    }
  }

  // Search through all mock data
  mockTreeData.forEach(rootNode => searchInNode(rootNode));

  return references;
}

interface ItemReferencessProps {
  node?: TreeNode;
}

export const ItemReferences: React.FC<ItemReferencessProps> = ({ node }) => {
  const references = React.useMemo(() => {
    if (!node) {
      return [];
    }
    return findReferencesToNode(node.id, node.name);
  }, [node]);

  return (
    <>
      <Typography variant='caption'>
        {references.length > 0
          ? `This asset is referenced in ${references.length} location(s)`
          : 'This asset is not referenced anywhere'
        }
      </Typography>
      {references.length > 0 && (
        <StyledTableContainer>
          <StyledTableRow>
            <StyledTableHeader>Asset</StyledTableHeader>
            <StyledTableHeader>Location</StyledTableHeader>
          </StyledTableRow>
          <StyledDivider />
          {references.map((reference, index) => (
            <StyledTableRow key={index}>
              <StyledTableCell>{reference.assetName}</StyledTableCell>
              <StyledTableCell>{reference.location}</StyledTableCell>
            </StyledTableRow>
          ))}
        </StyledTableContainer>
      )}
    </>
  );
};

const StyledTableContainer = styled(Box)(() => ({
  display: 'flex',
  flexDirection: 'column',
  border: '1px solid #555555',
  '& > div:nth-of-type(even)': {
    '& > div': {
      backgroundColor: '#292828',
    },
  },
}));

const StyledDivider = styled(Box)(() => ({
  height: '1px',
  backgroundColor: '#555555',
}));

const StyledTableRow = styled(Box)(() => ({
  display: 'flex',
  width: '100%',
}));

const StyledTableHeader = styled(Box)(() => ({
  backgroundColor: '#2d2d30',
  color: '#cccccc',
  fontSize: '10px',
  fontWeight: 500,
  padding: '4px 6px',
  flex: 1,
}));

const StyledTableCell = styled(Box)(() => ({
  backgroundColor: '#3c3c3c',
  color: '#cccccc',
  fontSize: '10px',
  padding: '2px 6px',
  flex: 1,
}));