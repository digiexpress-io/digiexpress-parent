import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Card
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import { FormattedMessage } from 'react-intl';

import { TemplateComposer, TemplateDelete, TemplateEdit } from './';
import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';



const TemplatesView: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const { site } = Composer.useComposer();
  const templates = Object.values(site.templates);


  const [templateComposer, setTemplateComposer] = React.useState(false);
  const [templateDelete, setTemplateDelete] = React.useState<StencilClient.TemplateId>();
  const [templateEdit, setTemplateEdit] = React.useState<StencilClient.TemplateId>();

  return (
    <>

      { templateComposer ? <TemplateComposer onClose={() => setTemplateComposer(false)} /> : null}
      { templateDelete ? <TemplateDelete templateId={templateDelete} onClose={() => setTemplateDelete(undefined)} /> : null}
      { templateEdit ? <TemplateEdit templateId={templateEdit} onClose={() => setTemplateEdit(undefined)} /> : null}

      <Box sx={{ paddingBottom: 1, m: 2 }}>
        <Box display="flex">
          <Box alignSelf="center">
            <Typography variant="h3" sx={{ p: 1, mb: 3, fontWeight: "bold", color: "mainContent.dark" }}>
              <FormattedMessage id="templates" />
              <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"templates.templatesview.description"} /></Typography>
            </Typography>
          </Box>
          <Box flexGrow={1} />
          <Box>
            <Burger.SecondaryButton label={"button.cancel"} onClick={() => layout.actions.handleTabCloseCurrent()} sx={{ marginRight: 1 }} />
            <Burger.PrimaryButton label={"button.create"} onClick={() => setTemplateComposer(true)} />
          </Box>
        </Box>

        <Card sx={{ margin: 1, width: 'fill-available' }}>
          <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
            <FormattedMessage id="templates" />
          </Typography>

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
                  <TableRow sx={{ p: 1 }} hover>
                    <TableCell align="left" sx={{ fontWeight: 'bold', width: "80px" }}>
                      <IconButton sx={{ color: 'uiElements.main' }} onClick={() => setTemplateEdit(template.id)}><EditIcon /></IconButton>
                    </TableCell>
                    <TableCell>{template.body.name}</TableCell>
                    <TableCell>{template.body.description}</TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold', width: "80px" }}>
                      <IconButton sx={{ color: 'uiElements.main' }} onClick={() => setTemplateDelete(template.id)}><DeleteOutlineOutlinedIcon /></IconButton>
                    </TableCell>
                  </TableRow>))
                }

              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </Box>
    </>

  );
}

export { TemplatesView }