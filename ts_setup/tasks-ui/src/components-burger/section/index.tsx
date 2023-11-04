import React from 'react';
import { Box } from '@mui/material';



const Section: React.FC<{ children: React.ReactNode, width?: string }> = (props) => {
  const [label, content] = React.Children.toArray(props.children);

  return (
    <Box width='100%'>
      <Box sx={{ zIndex: 10, marginBottom: "-11px", position: 'relative' }}>
        <Box display='flex' flexDirection='row'>
          <Box sx={{ pl: 2 }} />
          <Box sx={{ backgroundColor: 'primary.contrastText', px: 0.5, width: props.width }}>
            {label}
          </Box>
          <Box flexGrow={1} />
        </Box>
      </Box>

      <Box sx={{ borderRadius: '8px', border: 1, p: 2, borderColor: 'explorerItem.main' }}>
        {content}
      </Box>
    </Box>
  )
}

export { Section };