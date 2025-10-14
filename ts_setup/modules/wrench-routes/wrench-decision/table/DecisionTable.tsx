import React from 'react';

import { IconButton, Table, TableBody, TableCell, TableContainer, TablePagination, TableRow } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';

import { HdesApi } from '@dxs-ts/wrench-api';


interface RenderCellProps {
  row:HdesApi.AstDecisionRow;
  header:HdesApi.TypeDef;
  cell:HdesApi.AstDecisionCell;
}

interface RenderRowProps {
  headers:HdesApi.TypeDef[];
  row:HdesApi.AstDecisionRow;
  renderCell: (props: RenderCellProps) => React.ReactNode;
}

interface RenderHeaderProps {
  ast:HdesApi.AstDecision;
  headers:HdesApi.TypeDef[];
}

const DecisionTable: React.FC<{
  ast:HdesApi.AstDecision;
  renderHeader: (props: RenderHeaderProps) => React.ReactNode;
  renderRow: (props: RenderRowProps) => React.ReactNode;
  renderCell: (props: RenderCellProps) => React.ReactNode;
  onAddRow: () => void;

}> = ({ ast, renderRow, renderHeader, renderCell, onAddRow }) => {

  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(50);

  const handleChangePage = (_event: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };

  const accepts:HdesApi.TypeDef[] = ast ? [...ast.headers.acceptDefs].sort((a, b) => a.order - b.order) : [];
  const returns:HdesApi.TypeDef[] = ast ? [...ast.headers.returnDefs].sort((a, b) => a.order - b.order) : [];
  const rows = React.useMemo(() => ast ? ast.rows.sort((a, b) => a.order - b.order) : [], [ast]);
  const headers:HdesApi.TypeDef[] = [...accepts, ...returns];

  if (!ast) {
    return <span>syntax error</span>
  }

  return (<>
    <TableContainer sx={{
      height: "calc(100vh - 150px)"
    }}>
      <Table stickyHeader size="small">
        {renderHeader({ ast, headers })}
        <TableBody>
          {rows
            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
            .map((row) => (
              <React.Fragment key={row.id}>
                {renderRow({ row, renderCell, headers })}
              </React.Fragment>
            ))}

          <TableRow>
            <TableCell
              colSpan={headers.length + 2}
              align="left"
              sx={{
                pl: 0,
                borderBottom: "unset"
              }}
            >
              <IconButton
                color="primary"
                onClick={() => onAddRow()}
              >
                <AddIcon />
              </IconButton>
            </TableCell>
          </TableRow>

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

export type { RenderCellProps, RenderRowProps, RenderHeaderProps };
export { DecisionTable };
