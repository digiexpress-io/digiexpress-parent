import { BatchApi } from "@/api-batch";
import { useFetch } from "@dxs-ts/eveli-fetch";
import { alpha, Badge, Box, Chip, Paper, Stack, Theme, Typography, useTheme } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { DateTime, Interval } from "luxon";
import { useIntl } from "react-intl";
import numbro from 'numbro';
import { BatchViewHeaders } from "../eveli-batches-headers";
import { EveliBatchViewRoot, StyledInstanceSlot, useUtilityClasses } from "./useUtilityClasses";



interface SectionWidth {
  instanceSectionWidth: string,
  stepSectionWidth: string
}
const sectionWidth: SectionWidth = {
  instanceSectionWidth: '300px',
  stepSectionWidth: '200px'
}

export const EveliBatchView: React.FC<{ batchId: string }> = ({ batchId }) => {
  const classes = useUtilityClasses();
  const { getOne } = useFetch('worker/rest/api/batches.GET', {});

  const { data: batch, error, refetch, isPending } = useQuery({
    queryKey: ['batches/' + batchId],
    queryFn: () => getOne(batchId),
  });

  if (isPending || !batch) {
    return (<></>);
  }

  const instances = batch.transitives?.instances ?? [];

  return (
    <EveliBatchViewRoot className={classes.root}>
      <BatchViewHeaders batch={batch} instanceSectionWidth={sectionWidth.instanceSectionWidth} stepSectionWidth={sectionWidth.stepSectionWidth} />
      {instances.map(instance => (<InstanceSlot key={instance.id} value={instance} />))}
    </EveliBatchViewRoot>);
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


function getStepBackgroundColor(status: BatchApi.RuntimeStatus, theme: Theme): string {
  switch (status) {
    case 'CANCELLED': {
      return `${alpha(theme.palette.action.disabled, 0.1)}`
    }
    case 'COMPLETED': {
      return `${alpha(theme.palette.success.main, 0.1)}`
    }
    //TODO
    case 'CREATED': {
      return ''
    }
    //TODO
    case "EXECUTING": {
      return ''
    }
    case 'SKIPPED': {
      return `${alpha(theme.palette.action.disabled, 0.05)}`
    }
    default: {
      return theme.palette.background.paper;
    }
  }

}


const StepSlot: React.FC<{ value: BatchApi.RuntimeStep, instance: BatchApi.RuntimeInstance }> = ({ value, instance }) => {
  const interval = Interval.fromDateTimes(
    DateTime.fromISO(value.createdAt),
    DateTime.fromISO(value.endedAt ?? value.createdAt)
  )
  const duration = numbro(interval.length('minutes')).format({
    thousandSeparated: true,
    mantissa: 0,
  });
  const theme = useTheme();

  const bg_color = getStepBackgroundColor(value.status, theme)

  const metric = instance.transitives?.metrics
    .filter(metric => metric.name === 'batch-metrics')
    .find(metric => metric.stepId === value.id);

  const format = `~ ${duration} min.`;
  return (
    <Paper sx={{ padding: 2, width: sectionWidth.stepSectionWidth, backgroundColor: bg_color }}>
      <div>{format}</div>
      <div>Status: {value.status}</div>
      {value.status !== 'SKIPPED' && (
        <>
          <div>success: {metric?.valueStructured?.map.successCount}</div>
          <div>fail: {metric?.valueStructured?.map.failCount}</div>
        </>
      )}
    </Paper>
  )
}


const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const { locale } = useIntl();
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale(locale);
  const formatted_date = dateTime.toLocaleString(DateTime.DATE_SHORT);
  const formatted_time = dateTime.toLocaleString(DateTime.TIME_24_WITH_SECONDS);

  return (
    <Stack direction='column'>
      <Typography variant='subtitle2'>{formatted_date}</Typography>
      <Typography variant='subtitle2'>{formatted_time}</Typography>
    </Stack>);
}
