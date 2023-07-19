import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Card
} from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';


import Burger from '@the-wrench-io/react-burger';

import { FormattedMessage } from 'react-intl';
import { Composer, Client } from '../context';


const PreviewContextDialog: React.FC<{
  form: Client.Form, 
  onChange: (props: {key: string, value?: string}) => void, 
  ctx: Record<string, string>
}> = ({ form, onChange, ctx }) => {
  
  const contextVariables = form.data.variables ? form.data.variables.filter(v => v.context === true) : [];
  
  const rows = contextVariables.map((v, key) => <TableRow key={key}>
    <TableCell>
      <DeleteOutlineIcon onClick={(_e) => onChange({key: v.name, value: undefined})} />
      <span>{v.name}</span>
    </TableCell>
    <TableCell>
      <Burger.TextField label="Value"
        placeholder={v.defaultValue}
        value={ctx[v.name] ?? ''}
        onChange={(value) => onChange({key: v.name, value})}/>
    </TableCell>
  </TableRow>);

  return (<>
    <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
      Values for context variables
    </Typography>

    <TableContainer component={Paper}>
      <TableHead>
        <TableRow sx={{ p: 1 }}>
          <TableCell align="left" sx={{ fontWeight: 'bold' }}>ID</TableCell>
          <TableCell align="left" sx={{ fontWeight: 'bold' }}>Value</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>{rows}</TableBody>
  </TableContainer>
  </>);

}

export { PreviewContextDialog }

/*
const PreviewContextDialogConnected = connect(
  state => ({
    previewContextOpen: state.dialobComposer.editor && state.dialobComposer.editor.get('previewContextDialog'),
    variables: state.dialobComposer.form && state.dialobComposer.form.get('variables'),
    contextValues: state.dialobComposer.form && state.dialobComposer.form.getIn(['metadata', 'composer', 'contextValues']),
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE
  }), {
    hidePreviewContext,
    setContextValue,
    createPreviewSession
  }
)(PreviewContextDialog);

export {
  PreviewContextDialogConnected as default,
  PreviewContextDialog
};
*/
