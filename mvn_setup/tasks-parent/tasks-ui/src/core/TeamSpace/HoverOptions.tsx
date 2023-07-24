import React from 'react';
import { SxProps, Box, IconButton, Tooltip } from '@mui/material';
import NotesIcon from '@mui/icons-material/Notes';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import SubdirectoryArrowRightOutlinedIcon from '@mui/icons-material/SubdirectoryArrowRightOutlined';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';



const boxSx: SxProps = { ml: 1, alignItems: 'center', justifyItems: 'center' }
const iconButtonSx: SxProps = { fontSize: 'small', color: 'uiElements.main', p: 0.5 }

const IconButtonWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<>
    {React.Children.map(children, (el, index) => <IconButton key={index} sx={iconButtonSx}>{el}</IconButton>)}
  </>)
}

const HoverOptions: React.FC<{ active: boolean }> = () => {
  return (
    <Box display='flex' sx={boxSx}>
      <IconButtonWrapper>
        <Tooltip title="View description" placement="top" arrow><NotesIcon fontSize='small' /></Tooltip>
        <EditOutlinedIcon fontSize='small' />
        <SubdirectoryArrowRightOutlinedIcon fontSize='small' />
      </IconButtonWrapper>
    </Box>
  )
}

const HoverMenu: React.FC<{}> = () => {
  return (<IconButtonWrapper><MoreHorizOutlinedIcon fontSize='small' /></IconButtonWrapper>)
}

export { HoverOptions, HoverMenu };