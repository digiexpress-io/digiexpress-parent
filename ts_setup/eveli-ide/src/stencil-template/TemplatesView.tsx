import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Button
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import { FormattedMessage } from 'react-intl';

import { TemplateComposer, TemplateDelete, TemplateEdit } from '.';
import { StencilComposerApi as Composer } from '@/stencil-setup';
import { StencilApi } from '@/api-stencil';
import { useStencilNav } from '../stencil-nav';
import { EveliPermissions } from '@/eveli-permissions';
import { CancelButton } from '@/eveli-styles';




const TemplatesView: React.FC<{}> = () => {
  const { site } = Composer.useComposer();
  const templates = Object.values(site.templates);
  const [templateComposer, setTemplateComposer] = React.useState(false);
  const [templateDelete, setTemplateDelete] = React.useState<StencilApi.TemplateId>();
  const [templateEdit, setTemplateEdit] = React.useState<StencilApi.TemplateId>();
  const { onTabCurrentClose } = useStencilNav();


  return (<>
    {templateComposer ? <TemplateComposer onClose={() => setTemplateComposer(false)} /> : null}
    {templateDelete ? <TemplateDelete templateId={templateDelete} onClose={() => setTemplateDelete(undefined)} /> : null}
    {templateEdit ? <TemplateEdit templateId={templateEdit} onClose={() => setTemplateEdit(undefined)} /> : null}


    <Typography variant="h1" >
      <FormattedMessage id="templates" />
    </Typography>

    <Box display='flex' alignItems='center' my={1}>
      <Typography variant="body2"><FormattedMessage id={"templates.templatesview.description"} /></Typography>
      <Box flexGrow={1} />
      <CancelButton onClick={() => onTabCurrentClose()} />

      <EveliPermissions id='CREATE_STENCIL_ASSET'>
        <Button variant='contained' onClick={() => setTemplateComposer(true)} ><FormattedMessage id='button.create' /></Button>
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