import React from 'react';
import { Typography, Box, useThemeProps } from '@mui/material';
import { BatchViewHeadersRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

import { BatchApi } from '@/api-batch';
import { DateTime } from 'luxon';
import numbro from 'numbro';
import { useIntl } from 'react-intl';


export interface BatchViewHeadersProps {
  batch: BatchApi.Batch,
  instanceSectionWidth: string,
  stepSectionWidth: string
}


function formatDuration(duration: number) {

  const result = numbro(duration).format({
    thousandSeparated: true,
    mantissa: 0
  });

  return result;
}

export const BatchViewHeaders: React.FC<BatchViewHeadersProps> = (initProps) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const ownerState = {
    ...props
  }

  const stepNames = getUniqueStepNames(props.batch);
  const averageDurations = getAverageStepRuntime(props.batch);

  return (
    <BatchViewHeadersRoot className={classes.root} ownerState={ownerState}>
      <Box className={classes.instanceSection}>
        <Typography className={classes.title}>
          {intl.formatMessage({ id: 'eveli.batches.batchView.averageRunTime', defaultMessage: 'Average run time' })}
        </Typography>
      </Box>

      {stepNames.map(step => (
        <Box key={step} className={classes.stepSection}>
          <Typography className={classes.title}>{step}</Typography>
          <Typography>{formatDuration(averageDurations[step])}{intl.formatMessage({ id: 'eveli.batches.batchView.averageRunTime.ms', defaultMessage: 'ms' })}</Typography>
        </Box>
      ))}
    </BatchViewHeadersRoot>
  )
}

function getUniqueStepNames(batch: BatchApi.Batch) {
  const stepNames = new Set<string>();
  const instances = batch.transitives?.instances ?? [];

  for (const instance of instances) {
    const steps = instance.transitives?.steps ?? [];
    for (const step of steps) {
      if (step.name) {
        stepNames.add(step.name)
      }
    }
  }
  return Array.from(stepNames);
}

function getAverageStepRuntime(batch: BatchApi.Batch): Record<string, number> {
  const stats = (batch.transitives?.instances ?? [])
    .flatMap(instance => instance.transitives?.steps ?? [])
    .filter(step => step.name && step.createdAt && step.endedAt)
    .map(step => {
      const start = DateTime.fromISO(step.createdAt);
      const end = DateTime.fromISO(step.endedAt!);
      const duration = end.diff(start, "millisecond").milliseconds;

      return { start, end, duration, step }
    })
    .reduce<Record<string, number[]>>((stepDurations, current) => {
      const step = current.step;
      const duration = current.duration;

      if (!stepDurations[step.name]) {
        stepDurations[step.name] = [];
      }
      stepDurations[step.name].push(duration);
      return stepDurations;
    }, {})

  return Object.keys(stats)
    .map(stepName => {
      const step = stats[stepName];
      return { stepName, totalDuration: step.reduce((sum, duration) => sum + duration), count: step.length }
    })
    .reduce<Record<string, number>>((collector, stat) => {
      collector[stat.stepName] = stat.totalDuration ? stat.totalDuration / stat.count : 0;
      return collector;
    }, {});
}


