import React from 'react';
import { styled } from '@mui/material/styles';
import { Box } from '@mui/material';
import Tooltip, { TooltipProps, tooltipClasses } from '@mui/material/Tooltip';


const TooltipStyled = styled(({ className, ...props }: TooltipProps) => (
  <Tooltip {...props} classes={{ popper: className }} />
))(({ theme  }) => ({
  [`& .${tooltipClasses.tooltip}`]: {
    backgroundColor: theme.palette.secondary.contrastText,
    color: theme.palette.getContrastText,
    maxWidth: '75vw',
    fontSize: '12pt',
    padding: 20
  },
}));

export const StyledTooltip: React.FC<{ 
  title: React.ReactElement, 
  children: React.ReactElement 
}> = ({ title, children }) => {
  
  return (
    <TooltipStyled arrow title={title}>
      <Box>{children}</Box>
    </TooltipStyled>
  );
}
