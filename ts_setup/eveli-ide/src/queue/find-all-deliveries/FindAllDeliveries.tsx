import React from 'react';
import MaterialTable from '@material-table/core';

import { useIntl } from 'react-intl';
import { Box,  Typography } from '@mui/material';
import { DateTimeFormatter } from '@/burger';
import { useQueue, QueueApi } from '../queue-api';


export const FindAllDeliveries: React.FC<{}> = ({  }) => {
  const intl = useIntl();
    const { findAllQueueDeliveries } = useQueue();
  
    const [data, setData] = React.useState<{ delivery: QueueApi.Delivery, attempt: QueueApi.DeliveryAttempt}[]>();
  
    React.useEffect(() => {
      findAllQueueDeliveries()
        .then(data => data.flatMap( delivery => (delivery.attempts.map(attempt => ({ delivery, attempt}) ))))
        .then(setData)
    }, []);

  return (
      <MaterialTable
        title={<Typography variant='h1'>{intl.formatMessage({ id: 'queue.all_deliveries.title' })}</Typography>}
        columns={[
          {
            title: intl.formatMessage({ id: 'queue.all_deliveries.messageId' }),
            field: 'delivery.messageId',
            headerStyle: { fontWeight: 'bold' },
            defaultSort: 'asc'
          },
          {
            title: intl.formatMessage({ id: 'queue.all_deliveries.status' }),
            field: 'attempt.consumerStatus',
            filtering: false,
          },
          {
            title: intl.formatMessage({ id: 'queue.all_deliveries.consumerComment' }),
            field: 'attempt.consumerComment',
            filtering: false,
          },
          {
            title: intl.formatMessage({ id: 'queue.all_deliveries.created' }),
            field: 'createdAt',
            filtering: false,
            type: 'date',
            render: data => <DateTimeFormatter timestamp={data.attempt.createdAt} />,
            headerStyle: { fontWeight: 'bold' }
          },
        ]}
        options={{
          actionsColumnIndex: -1,
          debounceInterval: 500,
          padding: 'dense',
          filtering: false,
          maxColumnSort: 1,
          search: true,
          paging: false
        }}
        isLoading={false}
        data={data || []}
      />

  );
}
