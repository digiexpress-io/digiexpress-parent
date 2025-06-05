import React from 'react';
import { Box, CircularProgress } from '@mui/material';
import { Typography, Grid } from '@mui/material';


export interface SectionProps {
  children: React.ReactNode, 
  width?: string, 
  loadingValue?: any, 
  required?: boolean 
}

export const Section: React.FC<SectionProps> = (props) => {
  const children = React.Children.toArray(props.children);
  const [label] = children;
  const loadingEnabled: boolean = Object.keys(props).includes("loadingValue");
  const showLoader: boolean = props.loadingValue ? false : true;

  const formattedLabel = props.required ? (
    <Box sx={{ backgroundColor: 'primary.contrastText', px: 0.5, width: props.width, display: 'flex' }}>
      {label}
      {props.required && <span>&nbsp;*</span>}
    </Box>
  ) : (
    <Box sx={{ backgroundColor: 'primary.contrastText', px: 0.5, width: props.width }}>
      {label}
    </Box>
  );

  return (
    <Box width='100%'>
      <Box sx={{ zIndex: 10, marginBottom: "-11px", position: 'relative' }}>
        <Box display='flex' flexDirection='row'>
          <Box sx={{ pl: 2 }} />
          {formattedLabel}
          <Box flexGrow={1} />
        </Box>
      </Box>

      <Box sx={{ borderRadius: '8px', border: 1, p: 2, borderColor: 'primary.contrastText' }}>
        {children.splice(1)}
        {loadingEnabled && showLoader && < CircularProgress size='10pt' />}
      </Box>
    </Box>
  )
}




export const SectionRow: React.FC<{ label: React.ReactNode, value: string | number | undefined }> = ({ label, value }) => {

  return (
    <Grid container>
      <Grid item md={3} lg={3}>
        <Typography fontWeight='bolder'>{label}</Typography>
      </Grid>
      <Grid item md={9} lg={9}>
        <Typography>{value}</Typography>
      </Grid>
    </Grid>
  )
}
