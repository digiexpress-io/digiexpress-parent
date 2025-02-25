import React from 'react';
import { Box, styled } from '@mui/material';

import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';

import { useNavigate } from 'react-router-dom';




const StyledToolbarButton = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'center',
  margin: theme.spacing(2),
  ':hover': {
    cursor: 'pointer'
  },
  '& .MuiSvgIcon-root': {
    color: theme.palette.primary.contrastText
  }
}));


const StyledToolbar = styled(Box)(({ theme }) => ({
  flexGrow: 1,
  display: 'flex',
  width: "100%",
  height: "100%",
  flexDirection: 'column',
  borderRight: `1px solid ${theme.palette.secondary.contrastText}`,
  backgroundColor: theme.palette.secondary.main
}))

export const Toolbar: React.FC<{}> = () => {
  const navigate = useNavigate();

  function handleBacktoTasks() {
    navigate('/ui/tasks');
  }
  return (
    <StyledToolbar>
      <StyledToolbarButton onClick={() => window.open("https://google.com", "_blank")}><HelpOutlineOutlinedIcon /></StyledToolbarButton>
      <StyledToolbarButton onClick={handleBacktoTasks}><HomeOutlinedIcon /></StyledToolbarButton>
    </StyledToolbar>
  );
}