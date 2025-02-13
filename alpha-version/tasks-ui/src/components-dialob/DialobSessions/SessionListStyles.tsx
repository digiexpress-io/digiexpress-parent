import { Stack, Box, useTheme } from '@mui/material';
import { SxProps } from '@mui/system';
import { wash_me, cyan } from 'components-colors';



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
}> = ({ onClick, children, index }) => {
  const theme = useTheme();

  function getStyles(index: number): SxProps {
    const isOdd = index % 2 === 1;

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
    <Box sx={getStyles(index)} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize} onClick={onClick}>
      {children}
    </Box>
  );
}


export { StyledStackItem, StyledStack }