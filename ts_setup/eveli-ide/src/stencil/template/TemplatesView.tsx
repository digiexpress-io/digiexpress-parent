import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Card, Button
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import { FormattedMessage } from 'react-intl';

import { TemplateComposer, TemplateDelete, TemplateEdit } from './';
import { Composer, StencilApi } from '../context';
import * as Burger from '@/burger';



const TemplatesView: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site } = Composer.useComposer();
  const templates = Object.values(site.templates);
  const [templateComposer, setTemplateComposer] = React.useState(false);
  const [templateDelete, setTemplateDelete] = React.useState<StencilApi.TemplateId>();
  const [templateEdit, setTemplateEdit] = React.useState<StencilApi.TemplateId>();

  return (<>
    {templateComposer ? <TemplateComposer onClose={() => setTemplateComposer(false)} /> : null}
    {templateDelete ? <TemplateDelete templateId={templateDelete} onClose={() => setTemplateDelete(undefined)} /> : null}
    {templateEdit ? <TemplateEdit templateId={templateEdit} onClose={() => setTemplateEdit(undefined)} /> : null}


    <Typography variant="h3" >
      <FormattedMessage id="templates" />
    </Typography>

    <Typography variant="body2"><FormattedMessage id={"templates.templatesview.description"} /></Typography>
    <Button onClick={() => layout.actions.handleTabCloseCurrent()} variant='text'><FormattedMessage id='button.cancel' /></Button>
    <Button variant='contained' onClick={() => setTemplateComposer(true)} ><FormattedMessage id='button.create' /></Button>

    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ p: 1 }}>
            <TableCell align="left" sx={{ fontWeight: 'bold' }} colSpan={2}><FormattedMessage id="template.name" /></TableCell>
            <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="template.desc" /></TableCell>
            <TableCell align="left"></TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {templates.map((template, index) => (
            <TableRow hover>
              <TableCell align="left" sx={{ fontWeight: 'bold', width: "80px" }}>
                <IconButton onClick={() => setTemplateEdit(template.id)}><EditIcon /></IconButton>
              </TableCell>
              <TableCell>{template.body.name}</TableCell>
              <TableCell>{template.body.description}</TableCell>
              <TableCell align="left" sx={{ fontWeight: 'bold', width: "80px" }}>
                <IconButton onClick={() => setTemplateDelete(template.id)}><DeleteOutlineOutlinedIcon /></IconButton>
              </TableCell>
            </TableRow>))
          }

        </TableBody>
      </Table>
    </TableContainer>


  </>

  );
}

export { TemplatesView }