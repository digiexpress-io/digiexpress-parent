import React from 'react';
import MaterialTable from '@material-table/core';

import { useIntl } from 'react-intl';
import { Typography } from '@mui/material';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';
import { useQueue, QueueApi } from '@dxs-ts/eveli-api';


export const FindAllMessages: React.FC<{}> = ({  }) => {
  const intl = useIntl();
    const { findAllQueueMessages } = useQueue();
  
    const [data, setData] = React.useState<QueueApi.QueueMessage[]>();
  
    React.useEffect(() => {
      findAllQueueMessages().then(setData)
    }, []);

  return (
      <MaterialTable
        title={<Typography variant='h1'>{intl.formatMessage({ id: 'queue.all_messages.title' })}</Typography>}
        columns={[
          {
            title: intl.formatMessage({ id: 'queue.all_messages.routingKey' }),
            field: 'routingKey',
            headerStyle: { fontWeight: 'bold' },
            defaultSort: 'asc'
          },
          {
            title: intl.formatMessage({ id: 'queue.all_messages.messageId' }),
            field: 'id',
            headerStyle: { fontWeight: 'bold' },
            defaultSort: 'asc'
          },
          {
            title: intl.formatMessage({ id: 'queue.all_messages.bodyId' }),
            field: 'bodyId',
            defaultSort: 'asc'
          },
          {
            title: intl.formatMessage({ id: 'queue.all_messages.bodyType' }),
            field: 'bodyType',
            defaultSort: 'asc'
          },
          {
            title: intl.formatMessage({ id: 'queue.all_messages.status' }),
            field: 'status',
            filtering: false,
          },
          {
            title: intl.formatMessage({ id: 'queue.all_messages.body' }),
            field: 'body',
            filtering: false,
            render: data => <>{JSON.stringify((data.bodyValue as any)["map"])}</>,
          },
          {
            title: intl.formatMessage({ id: 'queue.all_messages.created' }),
            field: 'createdAt',
            filtering: false,
            type: 'date',
            render: data => <DateTimeFormatter value={data.createdAt} />,
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
