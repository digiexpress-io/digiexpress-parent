import React from 'react';
import { Box, Typography, Card, Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';


import { FormattedMessage, useIntl } from 'react-intl';

import { LocalesOverview } from './LocalesOverview';
import * as Burger from '@/eveli-styles';
import { StencilComposerApi as Composer } from '@/stencil-setup';
import { StencilApi } from '@/api-stencil';
import { EveliPermissions } from '@/eveli-permissions';
import { CancelButton } from '@/eveli-styles';


const Header: React.FC<{ label: string }> = ({ label }) => {
  return (<TableCell sx={{ fontWeight: 'bold' }} align="left">
    <Typography sx={{ fontWeight: 'bold' }}>
      <FormattedMessage id={label} />
    </Typography>
  </TableCell>)
}

const LocalesView: React.FC<{}> = () => {
  const { enqueueSnackbar } = useSnackbar();
  const { site, service, actions } = Composer.useComposer();
  const [editLocale, setEditLocale] = React.useState<StencilApi.SiteLocale | undefined>();
  const locales = Object.values(site.locales);
  const title = useIntl().formatMessage({ id: "locales" });

  const handleEnable = (locale: StencilApi.SiteLocale, enabled: boolean) => {
    const entity: StencilApi.LocaleMutator = { localeId: locale.id, value: locale.body.value, enabled: enabled };
    console.log("entity", entity)
    service.update().locale(entity).then(success => {

      editLocale?.body.enabled ? enqueueSnackbar(message, { variant: 'info' }) : enqueueSnackbar(message, { variant: 'success' })

      console.log(success, message)
      setEditLocale(undefined);
      actions.handleLoadSite();
    });
  }

  let message: React.ReactNode;
  if (editLocale?.body.enabled) {
    message = <FormattedMessage id='snack.locale.disabled' />
  } else {
    message = <FormattedMessage id='snack.locale.enabled' />
  }

  const onClose = () => setEditLocale(undefined);
  return (<>
    {editLocale ?
      (<Dialog open={true} onClose={onClose}>
        <DialogTitle><FormattedMessage id={editLocale.body.enabled === true ? "locale.disable.title" : "locale.enable.title"} /></DialogTitle>
        <DialogContent>
          {editLocale.body.enabled ? <FormattedMessage id="locale.disable" /> : <FormattedMessage id="locale.enable" />}
        </DialogContent>
        <DialogActions>
          <CancelButton onClick={onClose} />
          <Button onClick={editLocale.body.enabled ? () => handleEnable(editLocale, false) : () => handleEnable(editLocale, true)}>
            <FormattedMessage id={editLocale.body.enabled ? "button.disable" : "button.enable"} />
          </Button>
        </DialogActions>
      </Dialog>) : null
    }

    <Typography variant="h1">{title}{": "}{locales.length}</Typography>
    <Typography variant="body2"><FormattedMessage id='locales.overview.description' /></Typography>

    <Box mb={1} />

    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <Header label="locale" />
            <Header label="status" />
          </TableRow>
        </TableHead>
        <TableBody >
          {locales.map((locale) => (
            <TableRow key={locale.id} hover>
              <TableCell align="left">{locale.body.value}</TableCell>
              <EveliPermissions id='EDIT_STENCIL_ASSET'>
                <TableCell>

                  <Burger.Switch
                    checked={locale.body.enabled}
                    onChange={() => setEditLocale(locale)}
                    label={undefined}
                    helperText={undefined}
                  />
                  {locale.body.enabled ? <FormattedMessage id="locales.enabledMessage" /> : <FormattedMessage id="locales.disabledMessage" />}
                </TableCell>
              </EveliPermissions>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
    <Box paddingTop={3} />
    <LocalesOverview site={site} />
  </>
  );
}

export { LocalesView }




