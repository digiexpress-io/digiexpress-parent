import React from 'react';
import { Typography } from '@mui/material';

import { EveliTableRoot, useUtilityClasses } from './useUtilityClasses';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { EveliTableColumnFilterDialog } from './EveliTableColumnFilterDialog';

export const EveliTable: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const classes = useUtilityClasses();
  return (
    <EveliTableRoot className={classes.root}>
      {children}
    </EveliTableRoot>
  )
}




// original, will soon not be used
export const EveliTableHeaderCell: React.FC<{ children: string, filterItems: string[] }> = ({ children, filterItems }) => {
  const [open, setOpen] = React.useState(false);

  return (<>
    <div className='headerCell'>
      <Typography>{children}</Typography>
      <div style={{ flexGrow: 1 }} />
      {/* <EveliTableColumnFilter filterItems={filterItems} /> */}
      <EveliTableColumnOptions onChooseCols={() => setOpen(true)} onSortAsc={() => { }} onSortDesc={() => { }} onClearSorting={() => { }} onClearColVisibility={() => { }} />
    </div>
  </>
  )
}
