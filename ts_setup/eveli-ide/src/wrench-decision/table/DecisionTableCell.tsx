import React from 'react';
import { Box, TableCell, Typography, useTheme, lighten, alpha, darken, Button, ButtonGroup, Tooltip } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

import { HdesApi } from '@/api-wrench';
import { CellEditIntl } from '../editors/CellEditIntl';
import IntlBuilder from '../editors/builders/TypeIntlBuilder';



const DecisionTableCell: React.FC<{
  dt: HdesApi.AstDecision,
  row: HdesApi.AstDecisionRow,
  header: HdesApi.TypeDef,
  cell: HdesApi.AstDecisionCell,
  onClick: () => void,
  onChange: (newCommands: HdesApi.AstCommand[]) => void
}> = ({ header, cell, onClick, dt, onChange }) => {
  const [openIntl, setOpenIntl] = React.useState<{ open: boolean, locale: string }>({ open: false, locale: '' });
  const theme = useTheme();
  const borderColor = theme.palette.mode === 'light' ? lighten(alpha(theme.palette.divider, 1), 0.88) : darken(alpha(theme.palette.divider, 1), 0.68);
  const edit = <EditIcon />;


  if (header.direction === "IN") {
    return (
      <TableCell key={cell.header} onClick={() => onClick()} sx={{ cursor: "pointer", borderRight: `1px ${borderColor} solid` }}>
        <Typography noWrap display="flex">
          {cell?.value ? cell.value : <Box sx={{ fontWeight: "bold" }} component="span">{edit}</Box>}
        </Typography>
      </TableCell>);
  }

  if (header.valueType === "INTL") {

    function handleClose() {
      setOpenIntl(({ open: false, locale: '' }));
    }

    function handleOpen(locale: string) {
      setOpenIntl(({ open: true, locale }));
    }
    return (
      <TableCell key={cell.header} sx={{ cursor: "pointer", borderRight: `1px ${borderColor} solid` }}>
        {openIntl.open && <CellEditIntl dt={dt} cell={cell} locale={openIntl.locale} onClose={handleClose} onChange={onChange} />}
        <ButtonGroup variant='text'>
          {(header.valueSet ?? []).map(locale => (
            <Tooltip title={new IntlBuilder({ value: cell.value ?? '{}', header }).getLocaleValue(locale)} key={header.id}>
              <Button onClick={() => handleOpen(locale)}><Typography textTransform='uppercase' fontWeight='bold'>{locale}</Typography></Button>
            </Tooltip>
          ))}
        </ButtonGroup>
      </TableCell>);
  }

  return (<TableCell
    key={cell.header}
    onClick={() => onClick()}
    sx={{
      cursor: "pointer",
      borderRight: `1px ${borderColor} solid`,
    }}>
    <Typography noWrap>
      {cell?.value ? cell?.value : edit}
    </Typography>

  </TableCell>);
}
export { DecisionTableCell };
