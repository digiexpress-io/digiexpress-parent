import React from 'react';
import { Card, CardContent } from '@mui/material';



// Main Calendar Component
export const CardWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <Card variant="outlined">
      <CardContent>
        {children}
      </CardContent>
    </Card>
  );
};