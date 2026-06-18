import React from 'react';
import { IconButton, Table, TableBody, TableCell, TableContainer, TablePagination, TableRow } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { Add as AddIcon } from '@mui/icons-material';


interface RenderCellProps {
  row: Fs.DecisionAstRow;
  header: Fs.TypeDef;
  cell: Fs.DecisionAstCell;
}

interface RenderRowProps {
  headers: Fs.TypeDef[];
  row: Fs.DecisionAstRow;
  renderCell: (props: RenderCellProps) => React.ReactNode;
}

interface RenderHeaderProps {
  ast: Fs.DecisionAst;
  headers: Fs.TypeDef[];
}

const DecisionTable: React.FC<{
  ast: Fs.DecisionAst;
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

  const accepts = ast ? [...ast.headers.acceptDefs].sort((a, b) => a.order - b.order) : [];
  const returns = ast ? [...ast.headers.returnDefs].sort((a, b) => a.order - b.order) : [];
  const rows = React.useMemo(() => ast ? [...ast.rows].sort((a, b) => a.order - b.order) : [], [ast]);
  const headers: Fs.TypeDef[] = [...accepts, ...returns];

  if (!ast) {
    return <span>syntax error</span>;
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
