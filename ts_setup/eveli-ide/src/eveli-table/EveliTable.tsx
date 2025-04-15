import React from 'react';
import { Box, Typography } from '@mui/material';
import { EveliTableColRoot, EveliTableHeaderRoot, EveliTableRoot, EveliTableRowRoot, useUtilityClasses } from './useUtilityClasses';

import { EveliTableDrawerButtonColumn } from './EveliTableDrawerButtonColumn';
import { EveliTableDrawerCols } from './EveliTableDrawerCols';
import { EveliTableDrawerFilters } from './EveliTableDrawerFilters';
import { EveliTableDrawer } from './EveliTableDrawer';

import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';

import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';


export const EveliTable: React.FC = () => {
  const classes = useUtilityClasses();
  const [colsMenuOpen, setColsMenuOpen] = React.useState(false);
  const [filtersMenuOpen, setFiltersMenuOpen] = React.useState(false);

  function toggleColsMenu() {
    setColsMenuOpen(prev => !prev);
    setFiltersMenuOpen(false);
  }

  function toggleFiltersMenu() {
    setFiltersMenuOpen(prev => !prev);
    setColsMenuOpen(false);
  }



  return (
    <Box sx={{ p: 2 }}> {/* mock container / wrapper */}
      <EveliTableRoot className={classes.root}>
        <EveliTableColRoot width='97%'>
          <EveliTableHeaderRoot className={classes.headerRow}>
            <EveliTableHeaderCell children='Priority' />
            <EveliTableHeaderCell children='Name' />
            <EveliTableHeaderCell children='Client' />
            <EveliTableHeaderCell children='Status' />
            <EveliTableHeaderCell children='Assignee' />
            <EveliTableHeaderCell children='Info' />
            <EveliTableHeaderCell children='Due' />
            <EveliTableHeaderCell children='Created' />
          </EveliTableHeaderRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='Amanda McGibbons' />
            <EveliTableCell children={<IndicatorStatus type='NEW' />} />
            <EveliTableCell children={<IndicatorAssignee name='John Cena' />} />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='MEDIUM' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='Gerald Bridgerton' />
            <EveliTableCell children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell children={
              <Box display='flex' gap={1}>
                <IndicatorAssignee name='Delana Yankee' />
                <IndicatorAssignee name='George Miller' />
              </Box>
            }
            />
            <EveliTableCell children='' />
            <EveliTableCell children='14.03.2025' />
            <EveliTableCell children='02.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell children='School application' />
            <EveliTableCell children='Pamela Anderson' />
            <EveliTableCell children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell children={<IndicatorAssignee name='Tina SuperExtraLongName' />} />
            <EveliTableCell children='Special notes...' />
            <EveliTableCell children='13.03.2025' />
            <EveliTableCell children='05.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='John Cena' />
            <EveliTableCell children={<IndicatorStatus type='COMPLETED' />} />
            <EveliTableCell children={<IndicatorAssignee name='Alan Wake' />} />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='LOW' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='John Cena' />
            <EveliTableCell children={<IndicatorStatus type='COMPLETED' />} />
            <EveliTableCell children={
              <Box display='flex' gap={1}>
                <IndicatorAssignee name='Sylvester Stallone' />
                <IndicatorAssignee name='Anthony Hopkins' />
                <IndicatorAssignee name='Al Pachino' />
              </Box>
            }
            />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='LOW' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='Tony McPizza' />
            <EveliTableCell children={<IndicatorStatus type='REJECTED' />} />
            <EveliTableCell children={<IndicatorAssignee name='Jerry Seinfeld' />} />
            <EveliTableCell children='Tough customer...' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell children={<IndicatorPriority type='MEDIUM' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='John Cena' />
            <EveliTableCell children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell children={<IndicatorAssignee name='Alan Wake' />} />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>
        </EveliTableColRoot>

        {colsMenuOpen && <EveliTableDrawer width='15%' children={<EveliTableDrawerCols />} />}
        {filtersMenuOpen && <EveliTableDrawer width='15%' children={<EveliTableDrawerFilters />} />}

        <EveliTableColRoot width='3%'>
          <EveliTableDrawerButtonColumn onColumnsClick={toggleColsMenu} onFiltersClick={toggleFiltersMenu} />
        </EveliTableColRoot>

      </EveliTableRoot>
    </Box>
  )
}

const EveliTableHeaderCell: React.FC<{ children: string }> = ({ children }) => {
  return (
    <div className='headerCell'>
      <Typography>{children}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter />
      <EveliTableColumnOptions />
    </div>
  )
}


const EveliTableCell: React.FC<{ children?: React.ReactNode | string }> = ({ children }) => {
  return (
    <div className='rowCell'>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}