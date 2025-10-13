import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, Link, Typography, useThemeProps } from '@mui/material';
import { EveliBatchStepRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { Close as CloseIcon } from '@mui/icons-material';
import { BatchApi } from '@dxs-ts/eveli-api';
import { FormattedMessage, useIntl } from 'react-intl';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useFetch } from '@dxs-ts/envir-fetch';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';


export interface EveliBatchStepProps {
  stepId: string;
}


export const EveliBatchStep: React.FC<EveliBatchStepProps> = (initProps) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { getOne } = useFetch('worker/rest/api/batches/steps/$stepId.GET', {})
  const [data, setData] = React.useState<BatchApi.RuntimeStep>();

  React.useEffect(() => {
    getOne(initProps.stepId).then(setData);
  }, []);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const ownerState = {
    ...props
  }

  if(!data) {
    return (<></>);
  }



  const columns: ColumnDef<BatchApi.RuntimeStepRow, any>[] = [
    {
      header: 'Order',
      accessorKey: 'rowNumber',
      size: 50,
      minSize: 50,
      enableSorting: false,
      enableResizing: true
    },
    {
      header: 'External id',
      accessorKey: 'externalId',
      size: 50,
      minSize: 50,
      enableSorting: false,
      enableResizing: true
    },
    {
      header: 'Health',
      accessorKey: 'health',
      accessorFn: (data) => data.executionStatus,
      size: 50,
      minSize: 50,
      enableSorting: false,
      enableResizing: true,
      cell: (created) => flexRender(HealthLink, { value: created.row.original })
    },
    {
      header: 'Created at',
      filterFn: 'includesString',
      accessorFn: (data) => data.createdAt,
      size: 170,
      minSize: 170,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (created) => flexRender(AnyDateTimeShort, { value: created.getValue() })
    },
    {
      header: 'Ended at',
      filterFn: 'includesString',
      accessorFn: (data) => data.endedAt,
      size: 170,
      minSize: 170,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (created) => flexRender(AnyDateTimeShort, { value: created.getValue() })
    },
    {
      header: 'Comment',
      accessorKey: 'comment',
      filterFn: 'includesString',
      size: 550,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },

  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          {data?.name}
        </Typography>
      </Box>
      <EveliBatchStepRoot className={classes.root} ownerState={ownerState}>
        <WithTableStyles data={data.transitives?.stepRows ?? []} columns={columns} options={{ tableId: 'batcheStepRows'}}/>
      </EveliBatchStepRoot>
    </Box>
  )
}


const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const intl = useIntl();

  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale(intl.locale);
  const formatted = dateTime.toLocaleString(DateTime.DATETIME_SHORT);
  return <div>{formatted}</div>;
}


const HealthLink: React.FC<{ value: BatchApi.RuntimeStepRow }> = ({ value }) => {
  const [open, setOpen] = React.useState<boolean>(false);  
  if (value.executionStatus === 'OK') {
    return (<>{value.executionStatus}</>)
  }


  function handleClose() {
    setOpen(false);
  }


  function handleOpen() {
    setOpen(true);
  }

  const stack = value.output?.map?.errorCause;

  return (
    <Box
      sx={{
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: 0,
      }}
    >
      <Link href="#" onClick={handleOpen}>
        {value.executionStatus}
      </Link>


      <Dialog onClose={handleClose} open={open} >
        <DialogTitle>{value.executionStatus}</DialogTitle>
        <IconButton
          onClick={handleClose}
          sx={(theme) => ({
            position: 'absolute',
            right: 8,
            top: 8,
            color: theme.palette.grey[500],
          })}
        >
          <CloseIcon />
        </IconButton>
        <DialogContent dividers>
          <Typography gutterBottom variant='body2'>
            {stack}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button autoFocus onClick={handleClose}>
            <FormattedMessage id='button.close'/>
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}


