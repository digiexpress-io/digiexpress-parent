import React from 'react';

import { Box, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { FormattedMessage } from 'react-intl'

import { Client } from '../../context';
import { cyan, turquoise_topaz } from 'components-colors';


const DecisionTableHeader: React.FC<{
  ast: Client.AstDecision,
  headers: Client.TypeDef[],
  children: React.ReactNode,
  onClick: (header: Client.TypeDef) => void
}> = ({ ast, headers, children, onClick }) => {
  const totalCols = ast.headers.returnDefs.length + ast.headers.acceptDefs.length + 1;

  return (<TableHead sx={{ position: "sticky", top: 0 }}>
    <TableRow>
      <TableCell align="left" colSpan={totalCols} sx={{ fontWeight: "bold", borderBottom: "unset", pl: 0 }}>
        <Box display="flex" alignItems="center">
          {children}
        </Box>
      </TableCell>
    </TableRow>
    <TableRow>
      <TableCell align="center"
        colSpan={ast.headers.acceptDefs.length + 1}
        sx={{ backgroundColor: turquoise_topaz, color: "primary.contrastText" }}>

        <Typography display="inline-flex" fontWeight="bold">
          <FormattedMessage id="decisions.table.inputs.title" />
        </Typography>
      </TableCell>

      <TableCell align="center" colSpan={ast.headers.returnDefs.length}
        sx={{ backgroundColor: cyan, color: "primary.contrastText" }}>

        <Typography display="inline-flex" fontWeight="bold">
          <FormattedMessage id="decisions.table.outputs.title" />
        </Typography>
      </TableCell>
    </TableRow>

    <TableRow>
      <TableCell align="left" sx={{ fontWeight: "bold", width: "30px", backgroundColor: turquoise_topaz, color: "primary.contrastText" }}>#</TableCell>
      {headers.map((accept) => (
        <TableCell key={accept.id} align="left"
          onClick={() => onClick(accept)}
          sx={{
            fontWeight: "bold", minWidth: "50px", maxWidth: "200px", cursor: "pointer",
            backgroundColor: accept.direction === "OUT" ? cyan : turquoise_topaz,
            color: "primary.contrastText"
          }}>
          
          <Box display="flex">
            <Box flexGrow="1">{accept.name}</Box>
            <Box><EditIcon /></Box>
          </Box>
        </TableCell>))}
    </TableRow>
  </TableHead>);
}

export type { };
export { DecisionTableHeader };
