import React from 'react';

import { Box, TableContainer, Table, TablePagination } from '@mui/material';

import client from '@taskclient';

import TableHeader from './MyWorkTableHeader';
import TableRows from './MyWorkTableRow';


const MyWork: React.FC<{}> = () => {
  const tasks = client.useTasks();
  const org = client.useOrg();

  const [loading, setLoading] = React.useState(true);
  const [content, setContent] = React.useState(new client.TablePaginationImpl<client.TaskDescriptor>({
    src: [],
    orderBy: 'created',
    sorted: false
  }).withRowsPerPage(10));

  React.useEffect(() => {
    const myTasks = tasks.state.tasksByOwner[org.state.iam.userId]?.filter(task => task.status === 'CREATED' || task.status === 'IN_PROGRESS');
    setContent(c => c.withSrc(myTasks));
    setLoading(false);

  }, [tasks, setContent, setLoading])

  return (
    <client.TableProvider>
      <Box sx={{ width: '100%' }}>
        <TableContainer>
          <Table size='small'>
            <TableHeader content={content} setContent={setContent} />
            <TableRows content={content} loading={loading} />
          </Table>
        </TableContainer>
        <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
          <Box alignSelf="center" flexGrow={1}></Box> {
            loading ? null :
              (<TablePagination
                rowsPerPageOptions={content.rowsPerPageOptions}
                component="div"
                count={content.src.length}
                rowsPerPage={content.rowsPerPage}
                page={content.page}
                onPageChange={(_event, newPage) => setContent(state => state.withPage(newPage))}
                onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent(state => state.withRowsPerPage(parseInt(event.target.value, 10)))}
              />)
          }
        </Box>

      </Box>
    </client.TableProvider>);
}

export { MyWork };
