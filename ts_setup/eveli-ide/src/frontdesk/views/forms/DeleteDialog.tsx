import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, Divider, Typography, DialogActions, Button, Dialog } from '@mui/material';

import { useFetch } from '@dxs-ts/eveli-fetch';


import { DialobFormEntry } from '../../types';
import { TableHeader } from '../../components/TableHeader';

interface DeleteDialogProps {
  deleteModalOpen: boolean;
  handleDeleteModalClose: () => void;
  formConfiguration: DialobFormEntry | undefined;
  refresh: ()=>void;
}

export const DeleteDialog: React.FC<DeleteDialogProps> = ({
  refresh,
  deleteModalOpen,
  handleDeleteModalClose,
  formConfiguration,
}) => {
  const intl = useIntl();
  const { deleteDialog } = useFetch('worker/rest/api/assets/dialob/proxy/forms/$formId.DELETE', {})

  return (
    <Box>
      <Dialog
        open={deleteModalOpen}
        onClose={handleDeleteModalClose}
        maxWidth={'lg'}
        sx={{ height: "50%", top: 70 }}
      >
        <DialogTitle sx={{ m: 0, p: "20px 20px" }}><TableHeader id='dialobForm.heading.deleteDialog' /></DialogTitle>
        <Divider />
        <DialogContent>
            <Typography sx={{padding: "20px 4px 4px 2px"}}><FormattedMessage id="dialobForm.dialog.deleteQuestion" values={{formName: formConfiguration?.metadata.label || intl.formatMessage({id: "dialobForms.dialog.emptyTitle"})}}/></Typography>
        </DialogContent>
        <Divider />
        <DialogActions sx={{display: "flex", justifyContent: "space-between", padding: "12px"}}>
            <Button onClick={handleDeleteModalClose} variant='contained' color='secondary'><FormattedMessage id={'button.cancel'} /></Button>
            <Button variant='contained' color='error'
                onClick={() => deleteDialog(formConfiguration?.id!, () => {
                  handleDeleteModalClose();
                  refresh();
                })}
            >
                <FormattedMessage id={'button.accept'} />
            </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}