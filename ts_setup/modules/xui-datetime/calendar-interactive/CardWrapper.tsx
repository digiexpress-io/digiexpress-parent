import React from 'react';
import { Card, CardContent } from '@mui/material';



// Main Calendar Component
export const CardWrapper: React.FC<{inline: boolean, children: React.ReactNode}> = ({ inline, children }) => {
  if(inline) {
    return <>{children}</>
  }
  return (
    <Card variant="outlined">
      <CardContent>
        {children}
      </CardContent>
    </Card>
  );
};