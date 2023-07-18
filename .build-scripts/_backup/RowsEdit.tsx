import React from 'react';

import { Box, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import { Client } from '../../context';
import { RowEdit, RowEditMode } from './RowEdit';


const DecisionHeader: React.FC<{
  decision: Client.AstDecision,
  headers: Client.TypeDef[],
  children: React.ReactNode
}> = ({ decision, headers, children }) => {
  const totalCols = decision.headers.returnDefs.length + decision.headers.acceptDefs.length + 1;

  return (<>
    <TableHead sx={{ position: "sticky", top: 0 }}>
      <TableRow>
        <TableCell align="left" colSpan={totalCols} sx={{ fontWeight: "bold", borderBottom: "unset", pl: 0 }}>
          <Box display="flex" alignItems="center">
            <Box>{children}</Box>
            <Box sx={{ ml: 1 }}>
              {decision.name} - <FormattedMessage id="decisions.table.hitpolicy" />: {decision.hitPolicy}
            </Box>
          </Box>
        </TableCell>
      </TableRow>
      <TableRow>
        <TableCell align="center" colSpan={decision.headers.acceptDefs.length + 1}
          sx={{ fontWeight: "bold", backgroundColor: "page.main", color: "primary.contrastText" }}>
          <FormattedMessage id="decisions.table.inputs.title" />
        </TableCell>
        <TableCell align="center" colSpan={decision.headers.returnDefs.length}
          sx={{ fontWeight: "bold", backgroundColor: "uiElements.main", color: "primary.contrastText" }}>
          <FormattedMessage id="decisions.table.outputs.title" />
        </TableCell>
      </TableRow>

      <TableRow>
        <TableCell align="left" sx={{ fontWeight: "bold", width: "30px", backgroundColor: "page.main", color: "primary.contrastText" }}>#</TableCell>
        {headers.map((accept) => (
          <TableCell key={accept.id} align="left"
            sx={{
              fontWeight: "bold", minWidth: "50px", maxWidth: "200px",
              backgroundColor: accept.direction === "OUT" ? "uiElements.main" : "page.main",
              color: "primary.contrastText"
            }}>
            {accept.name}
          </TableCell>))}
      </TableRow>
    </TableHead>
  </>);
}

const RowsEdit: React.FC<{
  ast: Client.AstDecision;
  header: React.ReactNode;
  
  onChange: (edit: RowEditMode) => void;
  renderRow: (props: {
    columns: Client.TypeDef[];
    row: Client.AstDecisionRow;
  }) => React.ReactNode;
  
}> = ({ ast, header, onChange }) => {

  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(50);

  const handleChangePage = (_event: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };

  const accepts: Client.TypeDef[] = ast ? [...ast.headers.acceptDefs].sort((a, b) => a.order - b.order) : [];
  const returns: Client.TypeDef[] = ast ? [...ast.headers.returnDefs].sort((a, b) => a.order - b.order) : [];
  const rows = React.useMemo(() => ast ? ast.rows.sort((a, b) => a.order - b.order) : [], [ast]);
  const headers: Client.TypeDef[] = [...accepts, ...returns];

  if (!ast) {
    return <span>syntax error</span>
  }

  return (<>
    <TableContainer sx={{
      height: "calc(100vh - 150px)"
    }}>
      <Table stickyHeader size="small">
        <DecisionHeader decision={ast} headers={headers} children={header} />
        <TableBody>
          {rows
            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
            .map((row) => <RowEdit
              headers={headers} row={row} key={row.id}
              onChange={(mode) => onChange(mode)} />)}
        </TableBody>
      </Table>
    </TableContainer>
    <TablePagination
      rowsPerPageOptions={[50, 100, 200]}
      component="div"
      count={rows.length}
      rowsPerPage={rowsPerPage}
      page={page}
      onPageChange={handleChangePage}
      onRowsPerPageChange={handleChangeRowsPerPage} />
  </>);
}

export type { };
export { RowsEdit };
