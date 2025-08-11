import React from 'react';
import { Tooltip } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import { useIntl } from 'react-intl';


import { BatchApi } from '@dxs-ts/eveli-api';
import { useUtilityClasses, getHealthConfig, BatchHealthType, BatchHealthBallRoot } from './useUtilityClasses';



export interface BatchHealthBallProps {
  batch: BatchApi.Batch;
}

export const BatchHealthBall: React.FC<BatchHealthBallProps> = ({ batch }) => {
  const intl = useIntl();
  const health: BatchHealthType = getHealthType(batch);
  const classes = useUtilityClasses(health);
  const tooltip = getHealthConfig(health);

  return (
    <BatchHealthBallRoot className={classes.root}>
      <Tooltip title={intl.formatMessage({ id: tooltip.intl })}>
        <CircleIcon className={classes.healthColor} />
      </Tooltip>
    </BatchHealthBallRoot>
  )
}

function getHealthType(batch: BatchApi.Batch): BatchHealthType {
  if(!batch.transitives || batch.transitives?.instances.length === 0) {
    return 'CREATED_NOT_RUN';
  }

  const [ last ] = batch.transitives.instances;
  
  // TODO
  // critical error

  // still executing
  if(last.status === 'EXECUTING' && last.executionStatus === 'OK') {
    return 'RUNNING_NO_FAILS'
  }
  if(last.status === 'EXECUTING' && last.executionStatus !== 'OK') {
    return 'RUNNING_SOME_FAILS'
  }

  // completed
  if(last.status === 'COMPLETED' && last.executionStatus === 'OK') {
    return 'COMPLETED_SUCCESS'
  }
  if(last.status === 'COMPLETED' && last.executionStatus !== 'OK') {
    return 'COMPLETED_SOME_FAILS'
  }

  

  return 'CREATED_NOT_RUN';
}