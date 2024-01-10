import { Stack, Theme, alpha, Box, darken, styled, Button, useTheme } from '@mui/material';
import { SxProps } from '@mui/system';
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


const StyledStackItem: React.FC<{
  index: number,
  children: React.ReactNode,
  onClick: () => void,
  active: boolean
}> = ({ active, onClick, children, index }) => {
  const theme = useTheme();

  function getStyles(index: number, active: boolean, theme: Theme): SxProps {
    const isOdd = index % 2 === 1;

    if (active) {
      return {
        p: 2,
        cursor: 'pointer',
        color: 'text.primary',
        backgroundColor: alpha(cyan, 0.3),
        fontWeight: 'bolder',
        borderRadius: 1
      };
    }
    if (isOdd) {
      return {
        p: 2,
        cursor: 'pointer',
        backgroundColor: cyan,
        color: 'text.primary'
      };
    } else {
      return {
        p: 2,
        cursor: 'pointer',
        backgroundColor: wash_me,
        color: 'text.primary'
      };
    }
  }

  return (
    <Box sx={getStyles(index, active, theme)} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize} onClick={onClick}>
      {children}
    </Box>
  );
}


export { StyledStackItem, StyledPreviewFIllButton, StyledEditDialobButton, StyledStack }