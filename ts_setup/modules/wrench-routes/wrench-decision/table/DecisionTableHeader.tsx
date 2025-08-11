import React from 'react';

import { Box, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { FormattedMessage } from 'react-intl'

import { HdesApi } from '@dxs-ts/wrench-api';


const DecisionTableHeader: React.FC<{
  ast:HdesApi.AstDecision,
  headers:HdesApi.TypeDef[],
  children: React.ReactNode,
  onClick: (header:HdesApi.TypeDef) => void
}> = ({ ast, headers, children, onClick }) => {
  const totalCols = ast.headers.returnDefs.length + ast.headers.acceptDefs.length + 2;

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
        sx={{ backgroundColor: "secondary.contrastText", color: "primary.contrastText" }}>

        <Typography display="inline-flex" fontWeight="bold">
          <FormattedMessage id="decisions.table.inputs.title" />
        </Typography>
      </TableCell>

      <TableCell align="center" colSpan={ast.headers.returnDefs.length}
        sx={{ backgroundColor: "primary.main", color: "primary.contrastText" }}>

        <Typography display="inline-flex" fontWeight="bold">
          <FormattedMessage id="decisions.table.outputs.title" />
        </Typography>
      </TableCell>
      <TableCell
        align="center"
        sx={{ backgroundColor: "secondary.contrastText", color: "primary.contrastText" }}
      />
    </TableRow>

    <TableRow>
      <TableCell align="left" sx={{ fontWeight: "bold", width: "30px", backgroundColor: "secondary.contrastText", color: "primary.contrastText" }}>#</TableCell>
      {headers.map((accept) => (
        <TableCell key={accept.id} align="left"
          onClick={() => onClick(accept)}
          sx={{
            fontWeight: "bold", minWidth: "50px", maxWidth: "200px", cursor: "pointer",
            backgroundColor: accept.direction === "OUT" ? "primary.main" : "secondary.contrastText",
            color: "primary.contrastText"
          }}>
          
          <Box display="flex">
            <Box flexGrow="1">{accept.name}</Box>
            <Box><EditIcon /></Box>
          </Box>
        </TableCell>))}
        <TableCell
          key="_delete"
          align="center"
          sx={{ backgroundColor: "secondary.contrastText", color: "primary.contrastText" }}
        >
        </TableCell>
    </TableRow>
  </TableHead>);
}

export type { };
export { DecisionTableHeader };
