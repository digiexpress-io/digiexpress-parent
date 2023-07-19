import React from 'react';
import { useTheme, useMediaQuery } from '@mui/material';

const Logo: React.FC<{}> = () => {
  
  const theme = useTheme();
  const small = useMediaQuery(theme.breakpoints.down("sm"));
  const medium = useMediaQuery(theme.breakpoints.down("md"));
  
  if(small) {
    return (<img src="./burger.png" height="70px" width="80px" />);
  } else if(medium) {
    return (<img src="./burger.png" height="100" width="120" />)
  }
    
  return (
    <img src="./burger.png" height="120px" width="150px" />
  )
}

export {Logo};