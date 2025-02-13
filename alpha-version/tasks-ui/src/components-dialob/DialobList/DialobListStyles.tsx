import { Stack, Theme, alpha, Box, darken, styled, Button, useTheme } from '@mui/material';
import { cyan, wash_me } from 'components-colors';


const StyledPreviewFIllButton = styled(Button)(() => ({
  color: wash_me,
  fontWeight: 'bold',
  backgroundColor: cyan,
  '&:hover': {
    backgroundColor: darken(cyan, 0.3),
  }
}));


const StyledEditDialobButton = styled(Button)(() => ({
  border: '1px solid',
  color: cyan,
  fontWeight: 'bold',
  borderColor: cyan,
  '&:hover': {
    borderColor: darken(cyan, 0.3),
    color: darken(cyan, 0.3)
  }
}));


const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    width: '23%',
    boxShadow: 5,
    paddingTop: theme.spacing(2),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box>);
}


export { StyledPreviewFIllButton, StyledEditDialobButton, StyledStack }