import React from 'react';
import { SxProps, Box, IconButton, Tooltip } from '@mui/material';
import NotesIcon from '@mui/icons-material/Notes';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import SubdirectoryArrowRightOutlinedIcon from '@mui/icons-material/SubdirectoryArrowRightOutlined';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';



const boxSx: SxProps = { ml: 1, alignItems: 'center', justifyItems: 'center' }
const iconButtonSx: SxProps = { fontSize: 'small', color: 'uiElements.main', p: 0.5 }

const IconButtonWrapper: React.FC<{ children: React.ReactNode, active: boolean }> = ({ children, active }) => {
  if (active) {
    return (<IconButton sx={iconButtonSx}>{children}</IconButton>)
  }

  return (<>{children}</>);
}

const HoverOptions: React.FC<{ active: boolean }> = ({ active }) => {
  return (
    <Box display='flex' sx={boxSx}>
      <IconButtonWrapper active={active}><Tooltip title="View description" placement="top" arrow><NotesIcon fontSize='small' /></Tooltip></IconButtonWrapper>
      <IconButtonWrapper active={active}><EditOutlinedIcon fontSize='small' /></IconButtonWrapper>
      <IconButtonWrapper active={active}><SubdirectoryArrowRightOutlinedIcon fontSize='small' /></IconButtonWrapper>
    </Box>
  )
}

const HoverMenu: React.FC<{ active: boolean }> = ({ active }) => {
  return (<IconButtonWrapper active={active}><MoreHorizOutlinedIcon fontSize='small' /></IconButtonWrapper>)
}

export { HoverOptions, HoverMenu };