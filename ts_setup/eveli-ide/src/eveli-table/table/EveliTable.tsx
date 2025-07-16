import React from 'react';


import { BodyRowSlot, HeaderRowSlot, FooterSlot, Root, useUtilityClasses, DrawerSlot, DrawerButtonBarSlot, DrawerButtonSlot } from './useUtilityClasses';
import { Box, Button, IconButton, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import FilterListOutlinedIcon from '@mui/icons-material/FilterListOutlined';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';

import { useIntl } from 'react-intl';
import { FillerRows } from './FillerRows';
import { EveliTenantFeatureEnabled } from '@/api-tenant-config';

export type EveliTableDrawerType = 'filters' | 'columns' | 'saved-filters' | 'export-data';

export interface EveliTableProps {
  slotProps: {
    root: {
      columnSizeVars: Record<string, number>
    },
    header: {
      cells: { width: number, title: React.ReactNode, subTitle: React.ReactNode }[];
    }
    body: {
      rows: { cells: { width: number, children: React.ReactNode }[] }[];
    },
    footer: {
      pageSize: number;
      children: React.ReactNode;
    }
    drawer: {
      body: Record<EveliTableDrawerType, React.ReactNode | undefined>;
    }
  }
}


export function EveliTable(props: EveliTableProps): React.ReactNode {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { header, body, footer, drawer, root } = props.slotProps;
  const [drawerOpen, setDrawerOpen] = React.useState<EveliTableDrawerType>();

  const handleDrawerClose = React.useCallback(() => setDrawerOpen(undefined), [])
  const handleDrawerOpenColumns = React.useCallback(() => setDrawerOpen('columns'), [])
  const handleDrawerOpenFilters = React.useCallback(() => setDrawerOpen('filters'), [])
  const handleDrawerOpenSavedFilters = React.useCallback(() => setDrawerOpen('saved-filters'), [])
  const handleDrawerExportData = React.useCallback(() => setDrawerOpen('export-data'), [])

  const drawerBody = drawerOpen ? drawer.body[drawerOpen] : undefined;


  return (
    <Box style={root.columnSizeVars} display='flex'>
      <Root className={classes.root}>
        <HeaderRowSlot className={classes.headerRow}>
          {header.cells.map((cell, index) => (
            <div key={index} className={classes.headerCell} style={{ width: cell.width }}>
              <Typography>{cell.title}</Typography>
              {cell.subTitle}
            </div>
          ))}
        </HeaderRowSlot>

        {body.rows.map((row, rowIndex) => (
          <BodyRowSlot className={classes.bodyRow} key={rowIndex}>
            {row.cells.map((cell, index) => (
              <div key={index} className={classes.bodyCell} style={{ width: cell.width }}>
                {(typeof cell.children) === 'string' ? <Typography>{cell.children}</Typography> : cell.children}
              </div>
            ))}
          </BodyRowSlot>
        ))}

        <FillerRows actualNumberOfRows={body.rows.length} minNumberOfRows={footer.pageSize} />

        <FooterSlot className={classes.footer}>
          {footer.children}
        </FooterSlot>

        {!!drawerBody && (
          <DrawerSlot className={classes.drawer}>
            <Box className='title'>{<Typography>{intl.formatMessage({ id: `eveli.table.drawer.title.${drawerOpen}`, defaultMessage: drawerOpen })}</Typography>}
              <IconButton onClick={handleDrawerClose}><CloseIcon fontSize='small' /></IconButton>
            </Box>
            {drawerBody}
          </DrawerSlot>
        )}
      </Root>

      <DrawerButtonBarSlot className={classes.drawerButtonBar}>
        <DrawerButtonSlot className={classes.drawerButton}>
          <Button variant='text' startIcon={<TableChartOutlinedIcon />} onClick={handleDrawerOpenColumns} disableRipple>
            <Typography>{intl.formatMessage({ id: 'eveli.table.drawer.buttons.colsButton', defaultMessage: 'Columns' })}</Typography>
          </Button>
        </DrawerButtonSlot>
        
        <DrawerButtonSlot className={classes.drawerButton}>
          <Button variant='text' startIcon={<FilterListOutlinedIcon />} onClick={handleDrawerOpenFilters} disableRipple>
            <Typography>{intl.formatMessage({ id: 'eveli.table.drawer.buttons.filtersButton', defaultMessage: 'Filters' })}</Typography>
          </Button>
        </DrawerButtonSlot>

        <EveliTenantFeatureEnabled id='SMART_TABLES'>
          <DrawerButtonSlot className={classes.drawerButton}>
            <Button variant='text' startIcon={<FavoriteBorderIcon />} onClick={handleDrawerOpenSavedFilters} disableRipple>
              <Typography>{intl.formatMessage({ id: 'eveli.table.drawer.buttons.savedFiltersButton', defaultMessage: 'Saved Filters' })}</Typography>
            </Button>
          </DrawerButtonSlot>
        </EveliTenantFeatureEnabled>

        { drawer.body['export-data'] && (
        <DrawerButtonSlot className={classes.drawerButton}>
          <Button variant='text' startIcon={<FilterListOutlinedIcon />} onClick={handleDrawerExportData} disableRipple>
            <Typography>{intl.formatMessage({ id: 'eveli.table.drawer.buttons.exportData', defaultMessage: 'Export data' })}</Typography>
          </Button>
        </DrawerButtonSlot>)
        }

      </DrawerButtonBarSlot>
    </Box>
  )
}