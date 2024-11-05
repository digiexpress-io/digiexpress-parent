import React from 'react';
import { Typography, Chip, Button } from '@mui/material';
import { COLORS } from './ColorMap';


type Props = {
  inactive?:boolean, 
  withLabel?:boolean, 
  color?: COLORS,
  children:any, 
  [x:string]:any
}

const Indicator:React.FC<Props> = ({ inactive, withLabel, children, color, ...restProps })=> {
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
      case COLORS.YELLOW: background = 'yellow'; break;
      case COLORS.BLUE: background = 'lightblue'; break;
      case COLORS.GREEN: background = 'lightgreen'; break;
      case COLORS.GREY: background = 'lightgrey'; break;
      case COLORS.RED: background = 'red'; break;
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

export default Indicator;
