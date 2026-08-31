import React from 'react';

import { useIntl } from 'react-intl';
import {
  Typography,
  TextField,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TableContainer,
  TableSortLabel,
  Paper,
  Box,
} from '@mui/material';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';
import { useQueue, QueueApi } from '@dxs-ts/eveli-api';

type Order = 'asc' | 'desc';
type SortableField = 'routingKey' | 'id' | 'bodyId' | 'bodyType';

export const FindAllMessages: React.FC<{}> = ({ }) => {
  const intl = useIntl();
  const { findAllQueueMessages } = useQueue();

  const [data, setData] = React.useState<QueueApi.QueueMessage[]>();
  const [search, setSearch] = React.useState('');
  const [order, setOrder] = React.useState<Order>('asc');
  const [orderBy, setOrderBy] = React.useState<SortableField>('routingKey');

  React.useEffect(() => {
    findAllQueueMessages().then(setData)
  }, []);

  const handleSort = (field: SortableField) => {
    if (orderBy === field) {
      setOrder(order === 'asc' ? 'desc' : 'asc');
    } else {
      setOrderBy(field);
      setOrder('asc');
    }
  };

  const filtered = (data || []).filter(row => {
    if (!search) {
      return true;
    }
    const needle = search.toLowerCase();
    return [row.routingKey, row.id, row.bodyId, row.bodyType, row.status]
      .some(value => String(value ?? '').toLowerCase().includes(needle));
  });

  const sorted = [...filtered].sort((a, b) => {
    const aValue = String(a[orderBy] ?? '');
    const bValue = String(b[orderBy] ?? '');
    const result = aValue.localeCompare(bValue);
    return order === 'asc' ? result : -result;
  });

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant='h1'>{intl.formatMessage({ id: 'queue.all_messages.title' })}</Typography>
        <TextField
          size='small'
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder={intl.formatMessage({ id: 'queue.all_messages.search' })}
        />
      </Box>
      <TableContainer component={Paper}>
        <Table size='small'>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>
                <TableSortLabel
                  active={orderBy === 'routingKey'}
                  direction={orderBy === 'routingKey' ? order : 'asc'}
                  onClick={() => handleSort('routingKey')}
                >
                  {intl.formatMessage({ id: 'queue.all_messages.routingKey' })}
                </TableSortLabel>
              </TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>
                <TableSortLabel
                  active={orderBy === 'id'}
                  direction={orderBy === 'id' ? order : 'asc'}
                  onClick={() => handleSort('id')}
                >
                  {intl.formatMessage({ id: 'queue.all_messages.messageId' })}
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'bodyId'}
                  direction={orderBy === 'bodyId' ? order : 'asc'}
                  onClick={() => handleSort('bodyId')}
                >
                  {intl.formatMessage({ id: 'queue.all_messages.bodyId' })}
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'bodyType'}
                  direction={orderBy === 'bodyType' ? order : 'asc'}
                  onClick={() => handleSort('bodyType')}
                >
                  {intl.formatMessage({ id: 'queue.all_messages.bodyType' })}
                </TableSortLabel>
              </TableCell>
              <TableCell>{intl.formatMessage({ id: 'queue.all_messages.status' })}</TableCell>
              <TableCell>{intl.formatMessage({ id: 'queue.all_messages.body' })}</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>{intl.formatMessage({ id: 'queue.all_messages.created' })}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sorted.map(row => (
              <TableRow key={row.id}>
                <TableCell>{row.routingKey}</TableCell>
                <TableCell>{row.id}</TableCell>
                <TableCell>{row.bodyId}</TableCell>
                <TableCell>{row.bodyType}</TableCell>
                <TableCell>{row.status}</TableCell>
                <TableCell>{JSON.stringify((row.bodyValue as any)["map"])}</TableCell>
                <TableCell><DateTimeFormatter value={row.createdAt} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
