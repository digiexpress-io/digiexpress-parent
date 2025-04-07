import React from 'react';
import { Box, Typography } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import FilterListIcon from '@mui/icons-material/FilterList';
import { EveliTableColRoot, EveliTableHeaderRoot, EveliTableRoot, EveliTableRowRoot, useUtilityClasses } from './useUtilityClasses';
import { VerticalButtonColumn } from './VerticalButtonColumn';


export const EveliTable: React.FC = () => {
  const classes = useUtilityClasses();
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

        <EveliTableColRoot width='3%'>
          <VerticalButtonColumn />
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
      <FilterListIcon fontSize='small' />
      <MoreVertIcon fontSize='small' />
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