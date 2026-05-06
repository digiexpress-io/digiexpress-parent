import React from 'react';
import { Table, TableBody, TableContainer, TablePagination } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';


interface RenderCellProps {
  row: Fs.DecisionAstRow;
  header: Fs.DecisionTypeDef;
  cell: Fs.DecisionAstCell | undefined;
}

interface RenderRowProps {
  headers: Fs.DecisionTypeDef[];
  row: Fs.DecisionAstRow;
  renderCell: (props: RenderCellProps) => React.ReactNode;
}

interface RenderHeaderProps {
  ast: Fs.DecisionAst;
  headers: Fs.DecisionTypeDef[];
}

const DecisionTable: React.FC<{
  ast: Fs.DecisionAst;
  renderHeader: (props: RenderHeaderProps) => React.ReactNode;
  renderRow: (props: RenderRowProps) => React.ReactNode;
  renderCell: (props: RenderCellProps) => React.ReactNode;
}> = ({ ast, renderRow, renderHeader, renderCell }) => {

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
  const headers: Fs.DecisionTypeDef[] = [...accepts, ...returns];

  if (!ast) {
    return <span>syntax error</span>;
  }

  return (<>
    <TableContainer sx={{ height: 'calc(100vh - 150px)' }}>
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
      onRowsPerPageChange={handleChangeRowsPerPage}
    />
  </>);
};

export type { RenderCellProps, RenderRowProps, RenderHeaderProps };
export { DecisionTable };
