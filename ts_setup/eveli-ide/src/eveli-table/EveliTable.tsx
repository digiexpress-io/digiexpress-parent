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
// for testing
export const EveliTableHeaderCell1: React.FC<{ children: any }> = ({ children }) => {
  const [open, setOpen] = React.useState(false);

  return (<>

    <div className='headerCell'>
      <Typography>{children}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter filterItems={['filter 1']} />
      <EveliTableColumnOptions onChooseCols={() => setOpen(true)} />
    </div>
  </>
  )
}
// original
export const EveliTableHeaderCell: React.FC<{ children: string, filterItems: string[] }> = ({ children, filterItems }) => {
  const [open, setOpen] = React.useState(false);

  return (<>
    <EveliTableColumnFilterDialog open={open} onClose={() => setOpen(false)} />
    <div className='headerCell'>
      <Typography>{children}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter filterItems={filterItems} />
      <EveliTableColumnOptions onChooseCols={() => setOpen(true)} />
    </div>
  </>
  )
}


export const EveliTableCell: React.FC<{ children?: React.ReactNode | string }> = ({ children }) => {
  return (
    <div className='rowCell'>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}