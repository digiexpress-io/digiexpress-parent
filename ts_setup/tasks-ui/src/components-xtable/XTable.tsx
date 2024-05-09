import React from 'react';
import { TableContainer as MTableContainer, Table as MTable } from '@mui/material';
import { XTableProvider } from './XTableContext';
import { useXPaper } from './XPaperContext';
import { XPrefProvider, useXPref } from './XPrefContext';
import { PreferenceInit } from 'descriptor-prefs';


interface XTableProps {
  rows: number;
  columns: number;
  hiddenColumns?: string[];
  children: React.ReactNode
}

export const XTable: React.FC<XTableProps> = ({ rows, columns, children, hiddenColumns }) => {
  const { uuid } = useXPaper();
  const init: PreferenceInit = React.useMemo(() => ({
    id: uuid,
    fields: []
  }), [uuid]);

  return (
    <XTableProvider rows={rows} columns={columns} hiddenColumns={hiddenColumns}>
      <XPrefProvider init={init}>
        <MTableContainer>
          <MTable size='small'>
            {children}
          </MTable>
        </MTableContainer>
      </XPrefProvider>
    </XTableProvider>
  );
}