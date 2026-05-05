import React from 'react';
import { Box, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';


const DecisionTableHeader: React.FC<{
  ast: any;
  headers: any[];
  children: React.ReactNode;
}> = ({ ast, headers, children }) => {
  const totalCols = ast.headers.returnDefs.length + ast.headers.acceptDefs.length + 2;

  return (
    <TableHead sx={{ position: 'sticky', top: 0 }}>
      <TableRow>
        <TableCell align="left" colSpan={totalCols} sx={{ fontWeight: 'bold', borderBottom: 'unset', pl: 0 }}>
          <Box display="flex" alignItems="center">
            {children}
          </Box>
        </TableCell>
      </TableRow>
      <TableRow>
        <TableCell
          align="center"
          colSpan={ast.headers.acceptDefs.length + 1}
          sx={{ backgroundColor: 'secondary.contrastText', color: 'primary.contrastText' }}
        >
          <Typography display="inline-flex" fontWeight="bold">
            <FormattedMessage id="decisions.table.inputs.title" />
          </Typography>
        </TableCell>
        <TableCell
          align="center"
          colSpan={ast.headers.returnDefs.length}
          sx={{ backgroundColor: 'primary.main', color: 'primary.contrastText' }}
        >
          <Typography display="inline-flex" fontWeight="bold">
            <FormattedMessage id="decisions.table.outputs.title" />
          </Typography>
        </TableCell>
        <TableCell align="center" sx={{ backgroundColor: 'secondary.contrastText', color: 'primary.contrastText' }} />
      </TableRow>
      <TableRow>
        <TableCell align="left" sx={{ fontWeight: 'bold', width: '30px', backgroundColor: 'secondary.contrastText', color: 'primary.contrastText' }}>#</TableCell>
        {headers.map((header: any) => (
          <TableCell key={header.id} align="left" sx={{
            fontWeight: 'bold',
            minWidth: '50px',
            maxWidth: '200px',
            backgroundColor: header.direction === 'OUT' ? 'primary.main' : 'secondary.contrastText',
            color: 'primary.contrastText',
          }}>
            {header.name}
          </TableCell>
        ))}
        <TableCell key="_delete" align="center" sx={{ backgroundColor: 'secondary.contrastText', color: 'primary.contrastText' }} />
      </TableRow>
    </TableHead>
  );
};

export { DecisionTableHeader };
