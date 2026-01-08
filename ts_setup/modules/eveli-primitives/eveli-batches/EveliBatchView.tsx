import React from "react";
import { BatchApi } from "@dxs-ts/eveli-api";
import { useFetch } from "@dxs-ts/envir-fetch";
import { Alert, AlertTitle, Box, Button, Chip, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';
import { DateTime, Interval } from "luxon";
import { useIntl } from "react-intl";
import numbro from 'numbro';
import { BatchViewHeaders } from "../eveli-batches-headers";
import { EveliBatchViewRoot, StyledInstanceSlot, StyledStepSlot, useUtilityClasses, sectionWidth } from "./useUtilityClasses";
import { StartBatchDialog } from "./StartBatchDialog";
import { useNavigate } from "@tanstack/react-router";



export const EveliBatchView: React.FC<{ batchId: string }> = ({ batchId }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [startDialogOpen, setStartDialogOpen] = React.useState(false);
  const { getOne } = useFetch('worker/rest/api/batches.GET', {});

  const { data: batch, error, refetch, isPending } = useQuery({
    queryKey: ['batches/' + batchId],
    queryFn: () => getOne(batchId),
    refetchInterval: 15000
  });

  if (isPending || !batch) {
    return (<></>);
  }

  const instances = batch.transitives?.instances ?? [];

  return (<>
    <StartBatchDialog batch={batch} open={startDialogOpen} onClose={() => {
      setStartDialogOpen(false);
      setTimeout(refetch, 2000)
    }} />
    <EveliBatchViewRoot className={classes.root}>
      <Box className={classes.batchNameRow}>
        <Typography variant='h1'>{batch.batchName}</Typography>
        <Button onClick={() => setStartDialogOpen(true)}>{intl.formatMessage({ id: 'button.startBatch' })}</Button>
      </Box>
      {!instances.length ? (<NoBatchInstancesAlert />
      ) : (
        <>
            <BatchViewHeaders batch={batch} instanceSectionWidth={sectionWidth.instanceSectionWidth} stepSectionWidth={sectionWidth.stepSectionWidth} />
            {instances.map(instance => (<InstanceSlot key={instance.id} value={instance} />))}
        </>
      )}
    </EveliBatchViewRoot>
  </>
  );
}

const InstanceSlot: React.FC<{ value: BatchApi.RuntimeInstance }> = ({ value }) => {
  const classes = useUtilityClasses();
  const instance = value;


  return (
    <StyledInstanceSlot className={classes.instanceSlot} ownerState={value}>
      <Paper className={classes.instanceContainer}>
        <Chip label={instance.name} size="small" />
        <Paper className={classes.instanceDateTime}>
          <AnyDateTimeShort value={instance.createdAt} />
        </Paper>
      </Paper>
      {value.transitives?.steps.map(step => (<StepSlot key={step.id} value={step} instance={instance} />))}
    </StyledInstanceSlot>

  )
}


const StepSlot: React.FC<{ value: BatchApi.RuntimeStep, instance: BatchApi.RuntimeInstance }> = ({ value, instance }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const nav = useNavigate();

  function handleOpenStep() {
    nav({
      from: '/secured/$locale/worker/batches/$batchId',
      to: '/secured/$locale/worker/batches/$batchId/steps/$stepId',
      params: { stepId: value.id }
    })
  }

  const interval = Interval.fromDateTimes(
    DateTime.fromISO(value.createdAt),
    DateTime.fromISO(value.endedAt ?? value.createdAt)
  )
  const duration = numbro(interval.length('minutes')).format({
    thousandSeparated: true,
    mantissa: 0,
  });

  const metric = instance.transitives?.metrics
    .filter(metric => metric.name === 'batch-metrics')
    .find(metric => metric.stepId === value.id);

  const format = `~ ${duration} min.`;
  return (
    <StyledStepSlot className={classes.stepSlot} value={value} onClick={handleOpenStep}>
      <Typography>{format}</Typography>
      <Typography>
        {intl.formatMessage({ id: 'eveli.batches.batchView.stepStatus', defaultMessage: 'Status' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon', defaultMessage: ': ' })}
        {value.status}
      </Typography>
      {value.status !== 'SKIPPED' && (
        <>
          <Typography>
            {intl.formatMessage({ id: 'eveli.batches.batchView.stepStatus.sucess', defaultMessage: 'Success' })}
            {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
            {metric?.valueStructured?.successCount}
          </Typography>

          <Typography>
            {intl.formatMessage({ id: 'eveli.batches.batchView.stepStatus.failure', defaultMessage: 'Fail' })}
            {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
            {metric?.valueStructured?.failCount}
          </Typography>
        </>
      )}
    </StyledStepSlot>

  )
}

const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  return <DateTimeFormatter value={value} withSeconds />;
};

const NoBatchInstancesAlert: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  return (
    <Alert severity='info' className={classes.noRunsAlert}>
      <AlertTitle>{intl.formatMessage({ id: 'eveli.batches.batchView.noRuns', defaultMessage: 'This batch has never been run' })}</AlertTitle>
      <Typography>{intl.formatMessage({ id: 'eveli.batches.batchView.startFirstRun', defaultMessage: 'Stats will appear here after the first run has been initialised' })}</Typography>
    </Alert>
  )
}
