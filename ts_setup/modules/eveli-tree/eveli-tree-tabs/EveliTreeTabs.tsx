import React from 'react';
import { Box, IconButton, styled, Tooltip, Typography } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';




export const EveliTreeTabs: React.FC = () => {
  const [activeIndex, setActiveIndex] = React.useState(0);

  const handleTabClick = (index: number) => {
    setActiveIndex(index);
  };

  const tabs = [
    'general-message.dialob',
    'info-gdpr.article',
    'fi'
  ];

  return (
    <StyledTabContainer>
      {tabs.map((tabName, index) => (
        <StyledTab key={tabName} isActive={activeIndex === index}
          onClick={() => handleTabClick(index)}
          isFirst={index === 0}
          isLast={index === tabs.length - 1}
        >
          <Tooltip title={tabName} arrow enterDelay={700} placement="bottom">
            <Typography variant='subtitle2'
              sx={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                flex: 1,
                minWidth: 0
              }}>{tabName}</Typography>
          </Tooltip>
          <IconButton size="small" sx={{ ml: 0.5, p: 0.25 }} color='primary'>
            <CloseIcon fontSize="inherit" color='primary' />
          </IconButton>
        </StyledTab>
      ))}
    </StyledTabContainer>
  );
};

const StyledTab = styled(Box)<{
  isActive?: boolean;
  isFirst?: boolean;
  isLast?: boolean;
}>(({ theme, isActive, isFirst }) => ({
  display: 'flex',
  alignItems: 'center',
  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  minWidth: '10ch',
  maxWidth: '20ch',
  overflow: 'hidden',
  borderTop: `1px solid ${theme.palette.divider}`,
  borderLeft: isFirst ? `1px solid ${theme.palette.divider}` : 'none',
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: isActive ? `1px solid ${theme.palette.background.paper}` : 'none',
  backgroundColor: isActive ? theme.palette.background.paper : theme.palette.grey[200],
  cursor: 'pointer',
  marginBottom: isActive ? '-1px' : 0
}));

const StyledTabContainer = styled(Box)(({ theme }) => ({
  height: 25,
  display: 'flex',
  borderBottom: `1px solid ${theme.palette.divider}`, borderColor: 'divider',
  width: '100%'
}))
