import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Button
} from '@mui/material';
import { Edit as EditIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { StencilApi, StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { EveliPermissions } from '@dxs-ts/eveli-primitives';

import { TemplateComposer } from './TemplateComposer';
import { TemplateDelete } from './TemplateDelete';
import { TemplateEdit } from './TemplateEdit';




const TemplatesView: React.FC<{}> = () => {
  const { site } = Composer.useComposer();
  const templates = Object.values(site.articleTemplates);
  const [templateComposer, setTemplateComposer] = React.useState(false);
  const [templateDelete, setTemplateDelete] = React.useState<StencilApi.TemplateId>();
  const [templateEdit, setTemplateEdit] = React.useState<StencilApi.TemplateId>();


  return (<>
    {templateComposer ? <TemplateComposer onClose={() => setTemplateComposer(false)} /> : null}
    {templateDelete ? <TemplateDelete templateId={templateDelete} onClose={() => setTemplateDelete(undefined)} /> : null}
    {templateEdit ? <TemplateEdit templateId={templateEdit} onClose={() => setTemplateEdit(undefined)} /> : null}


    <Typography variant="h1" >
      <FormattedMessage id="templates" />
    </Typography>

    <Box display="flex" alignItems="center" justifyContent="space-between" my={1}>
      <Typography variant="body2" sx={{ mr: 2, flex: '1 1 auto' }}>
        <FormattedMessage id="templates.templatesview.description" />
      </Typography>

      <EveliPermissions id="CREATE_STENCIL_ASSET">
        <Button variant="contained" onClick={() => setTemplateComposer(true)}>
          <FormattedMessage id="button.create" />
        </Button>
      </EveliPermissions>
    </Box>

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
          {templates.map((template) => (
            <TableRow hover key={template.id}>
              <TableCell align="left" sx={{ fontWeight: 'bold', width: "80px" }}>
                <EveliPermissions id='EDIT_STENCIL_ASSET'>
                  <IconButton onClick={() => setTemplateEdit(template.id)}><EditIcon /></IconButton>
                </EveliPermissions>
              </TableCell>
              <TableCell>{template.body.name}</TableCell>
              <TableCell>{template.body.description}</TableCell>
              <TableCell align="left" sx={{ fontWeight: 'bold', width: "80px" }}>
                <EveliPermissions id='DELETE_STENCIL_ASSET'>
                  <IconButton onClick={() => setTemplateDelete(template.id)}><DeleteOutlineOutlinedIcon /></IconButton>
                </EveliPermissions>
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