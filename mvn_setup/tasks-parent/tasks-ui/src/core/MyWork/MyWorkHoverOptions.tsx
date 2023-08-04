import React from 'react';
import { SxProps, Box, IconButton, Button, Typography } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';
import { FormattedMessage } from 'react-intl';


const boxSx: SxProps = { ml: 1, alignItems: 'center', justifyItems: 'center' }
const iconButtonSx: SxProps = { fontSize: 'small', color: 'uiElements.main', p: 0.5 }

const IconButtonWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<>
    {React.Children.map(children, (el, index) => <IconButton key={index} sx={iconButtonSx}>{el}</IconButton>)}
  </>)
}

const StartWorkButton: React.FC<{}> = () => {
  return (<Button variant='outlined' color='inherit' sx={{ pr: 1 }} endIcon={<ArrowForwardIosOutlinedIcon />}>
    <Typography variant='caption'><FormattedMessage id='core.myWork.button.task.start' /></Typography>
  </Button>)
}


const MyWorkHoverOptions: React.FC<{ active: boolean }> = () => {

  return (
    <Box display='flex' sx={boxSx} width='100%'>
      <StartWorkButton />
    </Box>
  )
}
const HoverMenu: React.FC<{}> = () => {
  return (<IconButtonWrapper><MoreHorizOutlinedIcon fontSize='small' /></IconButtonWrapper>)
}



export { MyWorkHoverOptions, HoverMenu, StartWorkButton };