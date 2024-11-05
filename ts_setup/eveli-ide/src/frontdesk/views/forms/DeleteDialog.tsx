import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, Divider, Typography, DialogActions, Button, Dialog } from '@mui/material';
import { useConfig } from '../../context/ConfigContext';
import { DialobFormEntry } from '../../types';
import { useContext } from 'react';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { handleErrors } from '../../util/cFetch';
import { enqueueSnackbar } from 'notistack';
import { TableHeader } from 'frontdesk/components/TableHeader';

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
  const { serviceUrl } = useConfig();
  const session = useContext(SessionRefreshContext);

  const deleteDialog = async () => {
    let url = `${serviceUrl}rest/api/assets/dialob/proxy/forms/${formConfiguration?.id}`;
    return session.cFetch(`${url}`,{
      method: 'DELETE',
    })
    .then((response:Response)=>handleErrors(response))
    .then((response:Response) => response.json())
    .then ((json:any)=>{
        handleDeleteModalClose();
        refresh();
        return json;
    })
    .catch((error:any) => {
      enqueueSnackbar(intl.formatMessage({id: 'dialobForm.deleteFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
    });
  };

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
                onClick={() => deleteDialog()}
            >
                <FormattedMessage id={'button.accept'} />
            </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}