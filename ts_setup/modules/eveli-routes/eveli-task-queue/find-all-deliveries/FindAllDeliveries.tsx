import React from 'react';

import { useIntl } from 'react-intl';
import { useQueue, QueueApi } from '@dxs-ts/eveli-api';
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

type Row = { delivery: QueueApi.Delivery, attempt: QueueApi.DeliveryAttempt };
type Order = 'asc' | 'desc';
type SortableField = 'messageId' | 'consumerId' | 'status' | 'consumerComment';

const getField = (row: Row, field: SortableField) => {
  switch (field) {
    case 'messageId': return row.delivery.messageId;
    case 'consumerId': return row.delivery.consumerId;
    case 'status': return row.attempt.consumerStatus;
    case 'consumerComment': return row.attempt.consumerComment;
  }
};

export const FindAllDeliveries: React.FC<{}> = ({ }) => {
  const intl = useIntl();
  const { findAllQueueDeliveries } = useQueue();

  const [data, setData] = React.useState<Row[]>();
  const [search, setSearch] = React.useState('');
  const [order, setOrder] = React.useState<Order>('asc');
  const [orderBy, setOrderBy] = React.useState<SortableField>('messageId');

  React.useEffect(() => {
    findAllQueueDeliveries()
      .then(data => data.flatMap(delivery => (delivery.attempts.map(attempt => ({ delivery, attempt })))))
      .then(setData)
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
    return [row.delivery.messageId, row.delivery.consumerId, row.attempt.consumerStatus, row.attempt.consumerComment]
      .some(value => String(value ?? '').toLowerCase().includes(needle));
  });

  const sorted = [...filtered].sort((a, b) => {
    const aValue = String(getField(a, orderBy) ?? '');
    const bValue = String(getField(b, orderBy) ?? '');
    const result = aValue.localeCompare(bValue);
    return order === 'asc' ? result : -result;
  });

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant='h1'>{intl.formatMessage({ id: 'queue.all_deliveries.title' })}</Typography>
        <TextField
          size='small'
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder={intl.formatMessage({ id: 'queue.all_deliveries.search' })}
        />
      </Box>
      <TableContainer component={Paper}>
        <Table size='small'>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>
                <TableSortLabel
                  active={orderBy === 'messageId'}
                  direction={orderBy === 'messageId' ? order : 'asc'}
                  onClick={() => handleSort('messageId')}
                >
                  {intl.formatMessage({ id: 'queue.all_deliveries.messageId' })}
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'consumerId'}
                  direction={orderBy === 'consumerId' ? order : 'asc'}
                  onClick={() => handleSort('consumerId')}
                >
                  {intl.formatMessage({ id: 'queue.all_deliveries.consumerId' })}
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'status'}
                  direction={orderBy === 'status' ? order : 'asc'}
                  onClick={() => handleSort('status')}
                >
                  {intl.formatMessage({ id: 'queue.all_deliveries.status' })}
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'consumerComment'}
                  direction={orderBy === 'consumerComment' ? order : 'asc'}
                  onClick={() => handleSort('consumerComment')}
                >
                  {intl.formatMessage({ id: 'queue.all_deliveries.consumerComment' })}
                </TableSortLabel>
              </TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>{intl.formatMessage({ id: 'queue.all_deliveries.created' })}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sorted.map((row, index) => (
              <TableRow key={index}>
                <TableCell>{row.delivery.messageId}</TableCell>
                <TableCell>{row.delivery.consumerId}</TableCell>
                <TableCell>{row.attempt.consumerStatus}</TableCell>
                <TableCell>{row.attempt.consumerComment}</TableCell>
                <TableCell><DateTimeFormatter value={row.attempt.createdAt} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
