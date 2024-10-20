import React from 'react';
import { Box, CircularProgress } from '@mui/material';
import { blueberry_whip } from 'components-colors';



const Section: React.FC<{ children: React.ReactNode, width?: string, loadingValue?: any, required?: boolean }> = (props) => {
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

      <Box sx={{ borderRadius: '8px', border: 1, p: 2, borderColor: blueberry_whip }}>
        {children.splice(1)}
        {loadingEnabled && showLoader && < CircularProgress size='10pt' />}
      </Box>
    </Box>
  )
}

export { Section };