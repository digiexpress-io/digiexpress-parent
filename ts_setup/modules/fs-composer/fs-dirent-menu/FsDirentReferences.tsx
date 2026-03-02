import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { FsNode, useFs } from '@dxs-ts/fs-api';
import { FsColors } from '../fs-theme';

interface FsDirentReferencesProps {
  node?: FsNode;
}

export const FsDirentReferences: React.FC<FsDirentReferencesProps> = ({ node }) => {
  const { findReferencesToNode } = useFs();

  const references = React.useMemo(() => {
    if (!node) {
      return [];
    }
    return findReferencesToNode(node);
  }, [node, findReferencesToNode]);

  return (
    <>
      <Typography variant='caption'>
        {references.length > 0 ? `This asset is referenced in ${references.length} location(s)` : 'This asset is not referenced anywhere'}
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
  border: `1px solid ${FsColors.dark.border}`,
  '& > div:nth-of-type(even)': {
    '& > div': {
      backgroundColor: FsColors.dark.surface,
    },
  },
}));

const StyledDivider = styled(Box)(() => ({
  height: '1px',
  backgroundColor: FsColors.dark.border,
}));

const StyledTableRow = styled(Box)(() => ({
  display: 'flex',
  width: '100%',
}));

const StyledTableHeader = styled(Box)(() => ({
  backgroundColor: FsColors.dark.surface,
  color: FsColors.dark.text,
  fontSize: '10px',
  fontWeight: 500,
  padding: '4px 6px',
  flex: 1,
}));

const StyledTableCell = styled(Box)(() => ({
  backgroundColor: FsColors.dark.background,
  color: FsColors.dark.text,
  fontSize: '10px',
  padding: '2px 6px',
  flex: 1,
}));