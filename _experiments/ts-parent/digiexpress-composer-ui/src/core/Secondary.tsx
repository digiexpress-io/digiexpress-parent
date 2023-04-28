import React from 'react';
import { Tab, Box, TabProps, BoxProps, alpha } from '@mui/material';
import { styled } from "@mui/material/styles";
import { FormattedMessage } from 'react-intl';

import Explorer from './Explorer';

const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.explorerItem.main,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem',
  },
  "&.Mui-selected": {
    color: theme.palette.explorerItem.dark,
    backgroundColor: alpha(theme.palette.explorerItem.dark, .2),
  },
}));

const StyledBox = styled(Box)<BoxProps>(({ theme }) => ({
  borderBottom: `1px solid ${theme.palette.explorerItem.dark}`,
  width: '100%',  
}));


const Secondary: React.FC<{}> = () => {

  return (<Box sx={{ backgroundColor: "explorer.main", height: '100%', width: '100%' }}>
    <StyledBox>
      <StyledTab label={<FormattedMessage id="explorer.title" />} value='label' />
    </StyledBox>
    <Explorer />
  </Box>)
}
export { Secondary }


