import { DialobSession } from 'client';
import Table from 'table';


interface SessionListState {
  sessions?: DialobSession[] | undefined;
  count: number | undefined;
}


interface SessionListStateInit {
  sessions: SessionListState[];
}

const initTable = (records: DialobSession[]) => new Table.TablePaginationImpl<DialobSession>({
  src: records,
  orderBy: 'metadata',
  order: 'desc',
  sorted: true,
  rowsPerPage: 15,
});



export type { SessionListState, SessionListStateInit };
export { initTable }