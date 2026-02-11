import React from 'react';
import { Box, styled, Typography } from '@mui/material';

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}

const historyData: ItemHistoryEntry[] = [
  { user: 'Diana Hasselback', change: 'Updated content', date: '15.01.2025' },
  { user: 'office-staff', change: 'Modified labels', date: '14.01.2025' },
  { user: 'John Smith', change: 'Updated description', date: '13.01.2025' },
  { user: 'Diana Hasselback', change: 'Configuration changed', date: '12.01.2025' },
  { user: 'part-time staff', change: 'Content review', date: '10.01.2025' },
  { user: 'John Smith', change: 'Updated permissions', date: '08.01.2025' },
  { user: 'office-staff', change: 'Added labels', date: '05.01.2025' },
  { user: 'Diana Hasselback', change: 'Content updated', date: '03.01.2025' },
  { user: 'John Smith', change: 'Structure modified', date: '28.12.2024' },
  { user: 'System', change: 'File created', date: '20.12.2024' }
];

export const NodeHistory: React.FC = () => {
  return (
    <>
      <Typography variant='caption'>Recent changes to this item</Typography>
      <StyledTableContainer>
        <StyledTableRow>
          <StyledTableHeader>User</StyledTableHeader>
          <StyledTableHeader>Change</StyledTableHeader>
          <StyledTableHeader>Date</StyledTableHeader>
        </StyledTableRow>
        <StyledDivider />
        {historyData.map((entry, index) => (
          <StyledTableRow key={index}>
            <StyledTableCell>{entry.user}</StyledTableCell>
            <StyledTableCell>{entry.change}</StyledTableCell>
            <StyledTableCell>{entry.date}</StyledTableCell>
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