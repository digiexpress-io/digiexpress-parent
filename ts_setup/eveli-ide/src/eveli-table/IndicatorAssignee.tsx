import React from 'react';
import { Avatar, styled, Tooltip, Typography } from '@mui/material';


interface IndicatorAssigneeProps {
  name: string;
}

const getRandomDarkColor = () => {
  const hue = Math.floor(Math.random() * 360); 
  const saturation = 60 + Math.random() * 40;  
  const lightness = 20 + Math.random() * 20; 
  return `hsla(${hue}, ${saturation}%, ${lightness}%)`;
};


export const IndicatorAssignee: React.FC<IndicatorAssigneeProps> = ({ name }) => {

  const firstName = name.substring(0, name.indexOf(" "));
  const lastName = name.substring(name.indexOf(" ") + 1);
  const firstInitial = firstName.substring(0, 1);
  const secondInitial = lastName.substring(0, 1);

 
  return (
    <IndicatorAssigneeRoot>
      <Tooltip title={name} arrow>
        <Avatar variant='rounded'>
          <Typography>{firstInitial}{secondInitial}</Typography>
        </Avatar>
      </Tooltip>
    </IndicatorAssigneeRoot>
  )
}




const MUI_NAME = 'IndicatorAssigneeRootClassName';
const IndicatorAssigneeRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    '.MuiAvatar-root': {
      height: '25px',
      width: '25px',
      backgroundColor: getRandomDarkColor(),
    }
  };
})