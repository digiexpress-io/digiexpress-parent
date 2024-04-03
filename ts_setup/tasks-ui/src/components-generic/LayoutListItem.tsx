import { Theme, alpha, Box, useTheme } from '@mui/material';
import { SxProps } from '@mui/system';
import { cyan, cyan_mud } from 'components-colors';



export const LayoutListItem: React.FC<{
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
        backgroundColor: cyan_mud,
        color: 'text.primary'
      };
    } else {
      return {
        p: 2,
        cursor: 'pointer',
        backgroundColor: undefined,
        color: 'text.primary'
      };
    }
  }

  return (
    <Box sx={getStyles(index, active, theme)} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize} onClick={onClick}>
      {children}
    </Box>);
}