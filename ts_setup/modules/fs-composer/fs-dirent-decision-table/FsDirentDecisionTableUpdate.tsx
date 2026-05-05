import React from 'react';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { useUtilityClasses, FsDirentDecisionTableRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDecisionTableUpdateProps } from './FsDirentDecisionTableProps';
import { DecisionTable, DecisionTableHeader, DecisionTableRow, DecisionTableCell } from './table';


export const FsDirentDecisionTableUpdate: React.FC<FsDirentDecisionTableUpdateProps> = (props) => {
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentDecisionTableRoot className={classes.root} ownerState={ownerState}>
        {ownerState.decision && (
          <DecisionTable
            ast={ownerState.decision}
            renderHeader={({ ast, headers }) => (
              <DecisionTableHeader ast={ast} headers={headers}>
                <span />
              </DecisionTableHeader>
            )}
            renderRow={({ row, headers, renderCell }) => (
              <DecisionTableRow row={row} headers={headers} renderCell={renderCell} />
            )}
            renderCell={({ row, header, cell }) => (
              <DecisionTableCell row={row} header={header} cell={cell} />
            )}
          />
        )}

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
        <FsDirentButtonCreate />
      </div>
    </FsDirentDecisionTableRoot>
  );
};
