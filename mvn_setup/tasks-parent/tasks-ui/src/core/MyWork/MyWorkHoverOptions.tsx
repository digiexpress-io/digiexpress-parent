import React from 'react';
import { SxProps, Box, IconButton, Button, Typography } from '@mui/material';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';
import AttachFileOutlinedIcon from '@mui/icons-material/AttachFileOutlined';
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

const CRMNotifications: React.FC<{}> = () => {
  return (<Box display='flex' sx={boxSx}>
    <IconButtonWrapper>
      <AttachFileOutlinedIcon fontSize='small' />
      <EmailOutlinedIcon fontSize='small' />
    </IconButtonWrapper>
  </Box>)
}

const MyWorkHoverOptions: React.FC<{ active: boolean }> = () => {

  return (
    <Box display='flex' sx={boxSx} width='100%'>
      <StartWorkButton />
      <Box flexGrow={1} />
      <CRMNotifications />
    </Box>
  )
}
const HoverMenu: React.FC<{}> = () => {
  return (<IconButtonWrapper><MoreHorizOutlinedIcon fontSize='small' /></IconButtonWrapper>)
}



export { MyWorkHoverOptions, CRMNotifications, HoverMenu, StartWorkButton };