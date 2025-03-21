import React from 'react';
import { Typography, Chip, Button } from '@mui/material';
import { TaskApi } from '../api-task';


export interface TaskIndicatorProps {
  inactive?:boolean, 
  withLabel?:boolean, 
  color?: TaskApi.Colors,
  children:any, 
  [x:string]:any
}

export const TaskIndicator: React.FC<TaskIndicatorProps> = ({ inactive, withLabel, children, color, ...restProps })=> {
  const extraProps:any = restProps;
  
  let content:JSX.Element|null = null;
  let rootNode:JSX.Element|null = null;
  let showButton = false;
  if(extraProps.onClick) {
    showButton = true;
    if(withLabel) {
      content = children;
    }
  }
  else {
    if (withLabel) {
      rootNode = children;
    }
  }
  if (color) {
    let background;
    switch (color) {
      case TaskApi.Colors.YELLOW: background = 'yellow'; break;
      case TaskApi.Colors.BLUE: background = 'lightblue'; break;
      case TaskApi.Colors.GREEN: background = 'lightgreen'; break;
      case TaskApi.Colors.GREY: background = 'lightgrey'; break;
      case TaskApi.Colors.RED: background = 'red'; break;
    }
    extraProps['style'] = {backgroundColor: background};
  }

  if (!showButton) {
    // chip does not support children...
    return <Chip disabled={inactive} {...extraProps} label={<Typography variant="caption">{rootNode}</Typography>} />
  }

  return (
    <Button disabled={inactive} {...extraProps} label={rootNode}>
      { content }
    </Button>
  );
}
