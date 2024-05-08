import { TableCell, TableCellProps, styled } from '@mui/material';

const lineHeight = 30;

export const XTableCell = styled(TableCell)<TableCellProps>(({ theme }) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  height: lineHeight + 'px',

  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),

  paddingTop: theme.spacing(0),
  paddingBottom: theme.spacing(0),
}));

