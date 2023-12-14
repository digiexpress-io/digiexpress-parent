import React from 'react';
import Typography from '@material-ui/core/Typography';
import WarningRoundedIcon from '@material-ui/icons/WarningRounded';

export default function Title(props: { 
  children: React.ReactNode, 
  secondary?: boolean,
  disabled?: boolean,
  notification?: boolean}) {
    
  let color: 'primary' | 'secondary' | 'textSecondary' = props.secondary? "secondary" : "primary";
    
  if(props.disabled) {
    color = "textSecondary";
  }
  
  if(props.notification) {
    
    return (<div>
      <Typography color={color}>
        <WarningRoundedIcon />
      </Typography>
      <Typography gutterBottom>
        {props.children}
      </Typography>
    </div>);    
  }
  
  return (
    <Typography component="h2" variant="h6" color={color} gutterBottom>
      {props.children}
    </Typography>
  );
}
