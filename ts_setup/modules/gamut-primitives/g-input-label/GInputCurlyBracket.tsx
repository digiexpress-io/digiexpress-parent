import React from 'react'
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInputCurlyBracket';


export const GInputCurlyBracket: React.FC<{enabled: boolean | undefined}> = (props) => {


  const classes = useUtilityClasses();
  if(!props.enabled) {
    return (<></>);
  }
  
  return (<GInputCurlyBracketRoot className={classes.root}>
    <div className={classes.top} />
    <div className={classes.bottom} />
  </GInputCurlyBracketRoot>);
}





const GInputCurlyBracketRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => {
  return {
    [theme.breakpoints.down('md')]: {
      display: 'none'
    },
    height: '100%',
    width: '16px',
    marginRight: '-4px',
    '& .GInputCurlyBracket-top': {
      borderRight: `4px solid ${theme.palette.divider}`,
      height: theme.spacing(7),
      borderBottomRightRadius: '16px',
    },
    '& .GInputCurlyBracket-bottom': {
      height: `calc(100% - ${theme.spacing(7)})`,
      borderRight: `4px solid ${theme.palette.divider}`,
      borderTopRightRadius: '16px',
    }
  };
});


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    top: ['top'],
    bottom: ['bottom'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}