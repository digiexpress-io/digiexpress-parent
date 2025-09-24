import React from 'react';
import { Card, CardContent } from '@mui/material';



// Main Calendar Component
export const CardWrapper: React.FC<{inline: boolean, children: React.ReactNode}> = ({ inline, children }) => {
  if(inline) {
    return <>{children}</>
  }
  return (
    <Card variant="outlined" sx={{
      width: 'fit-content',     // Fits width to content
      height: 'fit-content',    // Fits height to content
      display: 'inline-block'   // Prevents full-width behavior
    }}>
      <CardContent>
        {children}
      </CardContent>
    </Card>
  );
};