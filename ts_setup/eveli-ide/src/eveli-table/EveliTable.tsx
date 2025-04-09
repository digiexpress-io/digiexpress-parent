import React from 'react';
import { Box, Typography } from '@mui/material';
import { EveliTableColRoot, EveliTableHeaderRoot, EveliTableRoot, EveliTableRowRoot, useUtilityClasses } from './useUtilityClasses';
import { EveliTableRightMenuButtonColumn } from './EveliTableRightMenuButtonColumn';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableRightMenu } from './EveliTableRightMenu';
import { EveliTableRightMenuCols } from './EveliTableRightMenuCols';
import { EveliTableRightMenuFilters } from './EveliTableRightMenuFilters';


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
            <EveliTableHeaderCell className={classes.headerCell} title='Header cell 1' />
            <EveliTableHeaderCell className={classes.headerCell} title='Header cell 2' />
            <EveliTableHeaderCell className={classes.headerCell} title='Header cell 3' />
          </EveliTableHeaderRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} title='Cell 1' />
            <EveliTableCell className={classes.rowCell} title='Cell 2' />
            <EveliTableCell className={classes.rowCell} title='Cell 3' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} title='Cell 1' />
            <EveliTableCell className={classes.rowCell} title='Cell 2' />
            <EveliTableCell className={classes.rowCell} title='Cell 3' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} title='Cell 1' />
            <EveliTableCell className={classes.rowCell} title='Cell 2' />
            <EveliTableCell className={classes.rowCell} title='Cell 3' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} title='Cell 1' />
            <EveliTableCell className={classes.rowCell} title='Cell 2' />
            <EveliTableCell className={classes.rowCell} title='Cell 3' />
          </EveliTableRowRoot>

          <EveliTableRowRoot className={classes.row}>
            <EveliTableCell className={classes.rowCell} title='Cell 1' />
            <EveliTableCell className={classes.rowCell} title='Cell 2' />
            <EveliTableCell className={classes.rowCell} title='Cell 3' />
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


const EveliTableCell: React.FC<{ title: string, className: string }> = ({ title, className }) => {
  return (
    <div className={className}>
      <Typography>{title}</Typography>
    </div>
  )
}