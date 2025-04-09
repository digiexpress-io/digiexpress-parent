import React from 'react';
import { Box, Typography } from '@mui/material';
import { EveliTableColRoot, EveliTableHeaderRoot, EveliTableRoot, EveliTableRowRoot, useUtilityClasses } from './useUtilityClasses';
import { EveliTableRightMenuButtonColumn } from './EveliTableRightMenuButtonColumn';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableRightMenu } from './EveliTableRightMenu';
import { EveliTableRightMenuCols } from './EveliTableRightMenuCols';
import { EveliTableRightMenuFilters } from './EveliTableRightMenuFilters';
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
            <EveliTableHeaderCell className={classes.headerCell} title='Priority' />
            <EveliTableHeaderCell className={classes.headerCell} title='Name' />
            <EveliTableHeaderCell className={classes.headerCell} title='Client' />
            <EveliTableHeaderCell className={classes.headerCell} title='Status' />
            <EveliTableHeaderCell className={classes.headerCell} title='Assignee' />
            <EveliTableHeaderCell className={classes.headerCell} title='Info' />
            <EveliTableHeaderCell className={classes.headerCell} title='Due' />
            <EveliTableHeaderCell className={classes.headerCell} title='Created' />
          </EveliTableHeaderRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell className={classes.rowCell} title='Send feedback' />
            <EveliTableCell className={classes.rowCell} title='Amanda McGibbons' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='NEW' />} />
            <EveliTableCell className={classes.rowCell} children={<IndicatorAssignee name='John Cena' />} />
            <EveliTableCell className={classes.rowCell} title='Private data included' />
            <EveliTableCell className={classes.rowCell} title='12.03.2025' />
            <EveliTableCell className={classes.rowCell} title='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='MEDIUM' />} />
            <EveliTableCell className={classes.rowCell} title='Send feedback' />
            <EveliTableCell className={classes.rowCell} title='Gerald Bridgerton' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell className={classes.rowCell} children={
              <Box display='flex' gap={1}>
                <IndicatorAssignee name='Delana Yankee' />
                <IndicatorAssignee name='George Miller' />
              </Box>
            }
            />
            <EveliTableCell className={classes.rowCell} title='' />
            <EveliTableCell className={classes.rowCell} title='14.03.2025' />
            <EveliTableCell className={classes.rowCell} title='02.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell className={classes.rowCell} title='School application' />
            <EveliTableCell className={classes.rowCell} title='Pamela Anderson' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell className={classes.rowCell} children={<IndicatorAssignee name='Tina SuperExtraLongName' />} />
            <EveliTableCell className={classes.rowCell} title='Special notes...' />
            <EveliTableCell className={classes.rowCell} title='13.03.2025' />
            <EveliTableCell className={classes.rowCell} title='05.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell className={classes.rowCell} title='Send feedback' />
            <EveliTableCell className={classes.rowCell} title='John Cena' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='COMPLETED' />} />
            <EveliTableCell className={classes.rowCell} children={<IndicatorAssignee name='Alan Wake' />} />
            <EveliTableCell className={classes.rowCell} title='Private data included' />
            <EveliTableCell className={classes.rowCell} title='12.03.2025' />
            <EveliTableCell className={classes.rowCell} title='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='LOW' />} />
            <EveliTableCell className={classes.rowCell} title='Send feedback' />
            <EveliTableCell className={classes.rowCell} title='John Cena' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='COMPLETED' />} />
            <EveliTableCell className={classes.rowCell} children={
              <Box display='flex' gap={1}>
                <IndicatorAssignee name='Sylvester Stallone' />
                <IndicatorAssignee name='Anthony Hopkins' />
                <IndicatorAssignee name='Al Pachino' />
              </Box>
            }
            />
            <EveliTableCell className={classes.rowCell} title='Private data included' />
            <EveliTableCell className={classes.rowCell} title='12.03.2025' />
            <EveliTableCell className={classes.rowCell} title='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='LOW' />} />
            <EveliTableCell className={classes.rowCell} title='Send feedback' />
            <EveliTableCell className={classes.rowCell} title='Tony McPizza' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='REJECTED' />} />
            <EveliTableCell className={classes.rowCell} children={<IndicatorAssignee name='Jerry Seinfeld' />} />
            <EveliTableCell className={classes.rowCell} title='Tough customer...' />
            <EveliTableCell className={classes.rowCell} title='12.03.2025' />
            <EveliTableCell className={classes.rowCell} title='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} children={<IndicatorPriority type='MEDIUM' />} />
            <EveliTableCell className={classes.rowCell} title='Send feedback' />
            <EveliTableCell className={classes.rowCell} title='John Cena' />
            <EveliTableCell className={classes.rowCell} children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell className={classes.rowCell} children={<IndicatorAssignee name='Alan Wake' />} />
            <EveliTableCell className={classes.rowCell} title='Private data included' />
            <EveliTableCell className={classes.rowCell} title='12.03.2025' />
            <EveliTableCell className={classes.rowCell} title='01.03.2025' />
          </EveliTableRowRoot>
        </EveliTableColRoot>

        {colsMenuOpen && <EveliTableRightMenu width='15%' children={<EveliTableRightMenuCols />} />}
        {filtersMenuOpen && <EveliTableRightMenu width='15%' children={<EveliTableRightMenuFilters />} />}

        <EveliTableColRoot width='3%'>
          <EveliTableRightMenuButtonColumn onColumnsClick={toggleColsMenu} onFiltersClick={toggleFiltersMenu} />
        </EveliTableColRoot>

      </EveliTableRoot>
    </Box>
  )
}

const EveliTableHeaderCell: React.FC<{ title: string, className: string }> = ({ title, className }) => {
  return (
    <div className={className}>
      <Typography>{title}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter />
      <EveliTableColumnOptions />
    </div>
  )
}


const EveliTableCell: React.FC<{ title?: string, className: string, children?: React.ReactNode }> = ({ title, className, children }) => {
  return (
    <div className={className}>
      {children ? children : <Typography>{title}</Typography>}
    </div>
  )
}