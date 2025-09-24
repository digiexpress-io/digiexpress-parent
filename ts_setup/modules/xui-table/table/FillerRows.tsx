import React from 'react';
import { BodyFillerRowSlot, useUtilityClasses } from './useUtilityClasses';


export const FillerRows: React.FC<{ actualNumberOfRows: number, minNumberOfRows: number }> = ({ actualNumberOfRows, minNumberOfRows }) => {
  const classes = useUtilityClasses();
  const emptyRowsToFill = minNumberOfRows - actualNumberOfRows; // always show a minimum of 4 rows

  let fillers: React.ReactNode[] = [];
  for (let i = 0; i < emptyRowsToFill; i++) {
    fillers.push(
      <BodyFillerRowSlot key={i} className={classes.bodyFillerRow} />
    )
  }
  return (<>{fillers}</>)
}
