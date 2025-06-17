import { BatchApi } from "@/api-batch";
import { useFetch } from "@dxs-ts/eveli-fetch";
import { alpha, Badge, Box, Grid2, Paper, Stack, useTheme } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { DateTime, Interval } from "luxon";
import { useIntl } from "react-intl";
import numbro from 'numbro';




export const EveliBatchView: React.FC<{ batchId: string }> = ({ batchId }) => {
  const { getOne } = useFetch('worker/rest/api/batches.GET', {});

  const { data: batch, error, refetch, isPending } = useQuery({
    queryKey: ['batches/'+ batchId],
    queryFn: () => getOne(batchId),
  });

  if(isPending || !batch) {
    return (<></>);
  }

  const instances = batch.transitives?.instances ?? [];

  return (<Stack spacing={2} direction='column'>
    {instances.map(instance => (<InstanceSlot key={instance.id} value={instance} />))}
  </Stack>);
}

const InstanceSlot: React.FC<{ value: BatchApi.RuntimeInstance }> = ({ value }) => {
  const { palette } = useTheme();
  const instance = value;
  const isOk = instance.executionStatus === 'OK' ;

  const bg_paper = isOk ? alpha(palette.error.light, 0.2) : undefined;

  return (
    <Stack direction='row'>
      <Paper sx={{padding: 2, backgroundColor: bg_paper, width: '300px'}}>
        <Badge badgeContent={instance.name}>
          <Paper sx={{ 
            width: '100px', height: '60px', borderRadius: '5px',
            display: 'flex', alignItems: 'center',  justifyContent: 'center',
          }} >
            <AnyDateTimeShort value={instance.createdAt}/>
          </Paper>
        </Badge>
      </Paper>

      {value.transitives?.steps.map(step => (<StepSlot value={step} instance={instance}/>))}
    </Stack>)
}



const StepSlot: React.FC<{ value: BatchApi.RuntimeStep, instance: BatchApi.RuntimeInstance  }> = ({ value, instance }) => {
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
    <Paper sx={{padding: 2, width: '200px'}}>
      <div>name: {value.name}</div>
      <div>{format}</div>
      <div>success: {metric?.valueStructured?.map.successCount}</div>
      <div>fail: {metric?.valueStructured?.map.failCount}</div>
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

  return (<Stack direction='column'>
      <div>{formatted_date}</div>
      <Box sx={{ textAlign: 'center' }}>{formatted_time}</Box>
    </Stack>);
}
