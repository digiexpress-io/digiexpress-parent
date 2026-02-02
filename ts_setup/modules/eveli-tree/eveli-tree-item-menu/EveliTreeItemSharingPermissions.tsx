import React from 'react';
import { Box, Divider, styled, Typography } from '@mui/material';

interface SharingPermission {
  name: string;
  privilege: string;
}

const permissions: SharingPermission[] = [
  { name: 'John Smith (Me)', privilege: 'Read & write' },
  { name: 'Diana Hasselback', privilege: 'Read & write' },
  { name: 'office-staff', privilege: 'read' },
  { name: 'part-time staff', privilege: 'read' },
  { name: 'everyone', privilege: 'read' }
];

export const EveliTreeItemSharingPermissions: React.FC = () => {
  return (<>
    <Typography variant='caption'>You can read and write</Typography>
    <StyledTableContainer>
      <StyledTableRow>
        <StyledTableHeader>Name</StyledTableHeader>
        <StyledTableHeader>Privilege</StyledTableHeader>
      </StyledTableRow>
      <StyledDivider />
      {permissions.map((permission, index) => (
        <StyledTableRow key={index}>
          <StyledTableCell>{permission.name}</StyledTableCell>
          <StyledTableCell>{permission.privilege}</StyledTableCell>
        </StyledTableRow>
      ))}
    </StyledTableContainer>
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

const StyledDivider = styled(Divider)(() => ({ borderColor: '#555555' }))

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