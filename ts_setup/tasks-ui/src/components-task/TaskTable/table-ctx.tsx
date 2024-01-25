import React from 'react';
import { Box, TablePagination, TableContainer, Table } from '@mui/material';
import { TaskDescriptor, Group } from 'descriptor-task';
import Pagination from 'table';
import { PopperProvider, usePopper } from 'descriptor-popper';

type TaskPagination = Pagination.TablePagination<TaskDescriptor>;

interface TableConfigProps {
  loading: boolean;
  group: Group
  content: TaskPagination,
  setContent: React.Dispatch<React.SetStateAction<TaskPagination>>,
}

interface TableProps {
  config: {
    Header: React.ElementType<TableConfigProps>;
    Rows: React.ElementType<TableConfigProps>;
  },
  data: {
    group: Group,
    defaultOrderBy: keyof TaskDescriptor,
    loading: boolean;
  }
}

const useTable = usePopper;

function CustomTable(props: TableProps) {
  const { loading, defaultOrderBy, group } = props.data;
  const { Header, Rows } = props.config;
  const { records } = group;

  const [content, setContent] = React.useState(new Pagination.TablePaginationImpl<TaskDescriptor>({
    src: records ?? [],
    orderBy: defaultOrderBy,
    sorted: false
  }));

  React.useEffect(() => {
    setContent((c: TaskPagination) => c.withSrc(records ?? []));
  }, [records, setContent]);

  return (<PopperProvider>
    <Box sx={{ width: '100%' }}>
      <TableContainer>
        <Table size='small'>
          <Header content={content} loading={loading} setContent={setContent} group={group} />
          <Rows content={content} loading={loading} setContent={setContent} group={group} />
        </Table>
      </TableContainer>
      <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
        <Box alignSelf="center" flexGrow={1}></Box> {
          loading ? null :
            (<TablePagination
              rowsPerPageOptions={content.rowsPerPageOptions}
              component="div"
              count={(records ?? []).length}
              rowsPerPage={content.rowsPerPage}
              page={content.page}
              onPageChange={(_event, newPage) => setContent((state: TaskPagination) => state.withPage(newPage))}
              onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent((state: TaskPagination) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
            />)
        }
      </Box>
    </Box>
  </PopperProvider>
  );
}



export { useTable, CustomTable };
export type { TableConfigProps };


