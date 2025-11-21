import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';

import Editor from '@monaco-editor/react';
import YAML from 'yaml';
import { useIntl } from 'react-intl';

const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}


export const ContractProductDescriptionDialog: React.FC<{ open: boolean, onClose: () => void, content: any }> = ({ open, onClose, content}) => {
  const intl = useIntl();
  
  return (
  <Dialog open={open} onClose={onClose} maxWidth='xl'>
    <DialogTitle>{intl.formatMessage({id: 'contractcard.product.description.view'})}</DialogTitle>
    <DialogContent>
      <Editor
          value={toYaml(content)}
          onChange={() => {}}
          defaultLanguage='yaml'
          height='90vh'
        />
    </DialogContent>
    <DialogActions>
      <Button onClick={onClose}>{intl.formatMessage({id: 'button.close'})}</Button>
    </DialogActions>
  </Dialog>
)
}