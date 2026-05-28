import React from 'react';
import { Box, Button, ButtonGroup, TableCell, Tooltip, Typography, alpha, darken, lighten, useTheme } from '@mui/material';
import { Edit as EditIcon } from '@mui/icons-material';
import { Fs } from '@dxs-ts/fs-api';
import { CellEditIntl } from '../editors/CellEditIntl';
import IntlBuilder from '../editors/builders/TypeIntlBuilder';


const DecisionTableCell: React.FC<{
  dt: Fs.DecisionAst;
  row: Fs.DecisionAstRow;
  header: Fs.DecisionTypeDef;
  cell: Fs.DecisionAstCell | undefined;
  onClick: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}> = ({ dt, header, cell, onClick, onChange }) => {
  const theme = useTheme();
  const [openIntl, setOpenIntl] = React.useState<{ open: boolean; locale: string }>({ open: false, locale: '' });

  const borderColor = theme.palette.mode === 'light'
    ? lighten(alpha(theme.palette.divider, 1), 0.88)
    : darken(alpha(theme.palette.divider, 1), 0.68);

  if (header.direction === 'IN') {
    return (
      <TableCell
        sx={{ borderRight: `1px ${borderColor} solid`, cursor: cell ? 'pointer' : undefined }}
        onClick={cell ? onClick : undefined}
      >
        <Typography noWrap>
          {cell
            ? (cell.value ?? <EditIcon fontSize="small" color="disabled" />)
            : <Box component="span" sx={{ color: 'text.disabled' }}>—</Box>}
        </Typography>
      </TableCell>
    );
  }

  if (header.valueType === 'INTL') {
    const builder = new IntlBuilder({ header, value: cell?.value ?? '' });
    return (
      <TableCell sx={{ borderRight: `1px ${borderColor} solid` }}>
        {openIntl.open && cell && (
          <CellEditIntl
            dt={dt}
            cell={cell}
            locale={openIntl.locale}
            onClose={() => setOpenIntl({ open: false, locale: '' })}
            onChange={onChange}
          />
        )}
        <ButtonGroup variant="text">
          {(header.valueSet ?? []).map((locale) => (
            <Tooltip key={locale} title={builder.getLocaleValue(locale)}>
              <Button onClick={cell ? () => setOpenIntl({ open: true, locale }) : undefined}>
                <Typography textTransform="uppercase" fontWeight="bold">{locale}</Typography>
              </Button>
            </Tooltip>
          ))}
        </ButtonGroup>
      </TableCell>
    );
  }

  return (
    <TableCell
      sx={{ borderRight: `1px ${borderColor} solid`, cursor: cell ? 'pointer' : undefined }}
      onClick={cell ? onClick : undefined}
    >
      <Typography noWrap>
        {cell
          ? (cell.value ?? <EditIcon fontSize="small" color="disabled" />)
          : <Box component="span" sx={{ color: 'text.disabled' }}>—</Box>}
      </Typography>
    </TableCell>
  );
};

export { DecisionTableCell };
