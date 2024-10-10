import React from 'react';

import { Button, Box, DialogTitle, Dialog, DialogContent, Divider, List, ListItem, ListItemButton, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import { FormattedMessage, useIntl } from 'react-intl';
import Portal from '@the-stencil-io/portal';

import { UI_LANGUAGES } from '../util';

interface AppLocaleSelectionDialogProps {
  open: boolean;
  onClose: () => void;

}

const SelectDialog: React.FC<AppLocaleSelectionDialogProps> = ({ open, onClose }) => {
  const site = Portal.useSite();
  const intl = useIntl();

  const handleLanguageSelect = (language: string) => {
    onClose();
    site.setLocale(language)

  };

  return (
    <Dialog open={open} sx={{ backdropFilter: 'blur(5px)' }}>
      <DialogTitle>
        <Box display="flex" justifyContent='space-between' alignItems='center'>
          <Box>
            <FormattedMessage id="locale.select" />
          </Box>
          <Box>
            <IconButton onClick={onClose}
              sx={{color: (theme) => theme.palette.secondary.main, ml: 2}}>
              <CloseIcon />
            </IconButton>
          </Box>
        </Box>
      </DialogTitle>
      <Divider />
      <DialogContent >
        <List>
          {
            UI_LANGUAGES.map((lang, index) => (
              <ListItem disablePadding key={index}>
                <ListItemButton key={index} disabled={lang === intl.locale} onClick={() => handleLanguageSelect(lang)}
                  sx={{ "&:disabled": { color: 'darkGrey' }, color: "primary.main", pt: 2, pb: 2, pl: 1, pr: 1 }}>
                  <FormattedMessage id={`locale.${lang}`} />
                </ListItemButton>
              </ListItem>
            ))
          }
        </List>
      </DialogContent>
    </Dialog>
  )
}


const Locale: React.FC<{}> = ({ }) => {
  const site = Portal.useSite();
  const [dialogOpen, setDialogOpen] = React.useState(false);

  return (
    <Box>
      <SelectDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <Box>
        <Button onClick={() => setDialogOpen(true)}
          sx={{ color: "primary.contrastText", p: 0 }}>
          <FormattedMessage id={`locale.${site.locale}`} />
        </Button>
      </Box>
    </Box>
  );
}

export { Locale };


