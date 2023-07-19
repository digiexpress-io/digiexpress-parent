import React from 'react';
import { Box, Typography, Card, DialogContentText } from '@mui/material';
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
import Burger from '@the-wrench-io/react-burger';
import { Composer, StencilClient } from '../context';


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
  const [editLocale, setEditLocale] = React.useState<StencilClient.SiteLocale | undefined>();
  const locales = Object.values(site.locales);
  const title = useIntl().formatMessage({ id: "locales" });

  const handleEnable = (locale: StencilClient.SiteLocale, enabled: boolean) => {
    const entity: StencilClient.LocaleMutator = { localeId: locale.id, value: locale.body.value, enabled: enabled };
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


  return (<>
    { editLocale ?
      (<Burger.Dialog open={true} onClose={() => setEditLocale(undefined)}
        backgroundColor="uiElements.main" title={editLocale.body.enabled === true ? "locale.disable.title" : "locale.enable.title"}
        submit={{
          title: editLocale.body.enabled ? "button.disable" : "button.enable",
          onClick: editLocale.body.enabled ? () => handleEnable(editLocale, false) : () => handleEnable(editLocale, true),
          disabled: false
        }}>

        <DialogContentText>
          {editLocale.body.enabled ? <FormattedMessage id="locale.disable" /> : <FormattedMessage id="locale.enable" />}
        </DialogContentText>
      </Burger.Dialog>) : null
    }

    <Box sx={{ paddingBottom: 1, m: 2 }}>
      <Box display="flex">
        <Box alignSelf="center">
          <Typography variant="h3" sx={{ fontWeight: 'bold', p: 1 }}>{title}{": "}{locales.length}</Typography>
        </Box>
      </Box>

      <Box sx={{ justifyContent: 'center' }}>
        <Card sx={{ margin: 1 }}>
          <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
            <FormattedMessage id="locales" />
          </Typography>
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
                    <TableCell>
                      <Burger.Switch
                        checked={locale.body.enabled}
                        onChange={() => setEditLocale(locale)}
                        label={undefined}
                        helperText={undefined}
                      />
                      {locale.body.enabled ? <FormattedMessage id="locales.enabledMessage" /> : <FormattedMessage id="locales.disabledMessage" />}

                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
        <LocalesOverview site={site} />
      </Box>
    </Box>

  </>
  );
}

export { LocalesView }




