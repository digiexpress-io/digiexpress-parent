import { TableCell, TableCellProps, styled } from '@mui/material';
import { useXTableBody } from './XTableBodyContext';
import { SortType } from './XTableHeader';

const lineHeight = 30;

const XTableCellBase = styled(TableCell)<TableCellProps>(({ theme }) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  height: lineHeight + 'px',

  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),

  paddingTop: theme.spacing(0),
  paddingBottom: theme.spacing(0),
}));


export const XTableCell: React.FC<{
  children?: React.ReactNode | undefined;
  width?: string | undefined;
  maxWidth?: string | undefined;
  colSpan?: number | undefined;
  align?: 'left' | undefined;
  sortDirection?: SortType | undefined;
  padding?: 'none' | undefined 
}> = ({ children, width, maxWidth, colSpan, sortDirection, padding: userPadding }) => {
  const { padding } = useXTableBody();

  return (<XTableCellBase sortDirection={sortDirection} 
    width={width} 
    colSpan={colSpan} 
    sx={{p: userPadding === 'none' ? 0 : padding, maxWidth }}>{children}</XTableCellBase>);
}
