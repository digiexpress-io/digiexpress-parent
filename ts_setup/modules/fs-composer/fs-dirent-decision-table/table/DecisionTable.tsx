import React from 'react';
import { Table, TableBody, TableContainer, TablePagination } from '@mui/material';


interface RenderCellProps {
  row: any;
  header: any;
  cell: any;
}

interface RenderRowProps {
  headers: any[];
  row: any;
  renderCell: (props: RenderCellProps) => React.ReactNode;
}

interface RenderHeaderProps {
  ast: any;
  headers: any[];
}

const DecisionTable: React.FC<{
  ast: any;
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

  const accepts: any[] = ast ? [...ast.headers.acceptDefs].sort((a: any, b: any) => a.order - b.order) : [];
  const returns: any[] = ast ? [...ast.headers.returnDefs].sort((a: any, b: any) => a.order - b.order) : [];
  const rows = React.useMemo(() => ast ? ast.rows.sort((a: any, b: any) => a.order - b.order) : [], [ast]);
  const headers: any[] = [...accepts, ...returns];

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
            .map((row: any) => (
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
