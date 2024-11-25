import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, TextField, Divider, Typography, FormHelperText, Dialog, Button } from '@mui/material';
import { Form, Formik } from 'formik';
import * as Yup from 'yup';
import { useConfig } from '../../context/ConfigContext';
import { DEFAULT_FORM, DialobForm, DialobFormEntry } from '../../types';
import { handleErrors } from '../../util/cFetch';
import { enqueueSnackbar } from 'notistack';
import { useContext } from 'react';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { TableHeader } from '../../components/TableHeader';

interface CreateDialogProps {
  createModalOpen: boolean;
  handleCreateModalClose: () => void;
  formConfiguration?: DialobFormEntry;
  refresh: ()=>void;
}

interface RestFormConfigurationType{
  name: string | undefined;
  label: string | undefined;
}

export const CreateDialog: React.FC<CreateDialogProps> = ({
  refresh,
  createModalOpen,
  handleCreateModalClose,
  formConfiguration,
}) => {
  const intl = useIntl();
  const { serviceUrl } = useConfig();
  const session = useContext(SessionRefreshContext);

  const tagFormSchema = () => Yup.object().shape({
    name: Yup.string().required(intl.formatMessage({id: "error.valueRequired"})).matches(/^[_\-a-zA-Z\d]*$/g,intl.formatMessage({id: "dialobForm.error.invalidFormName"})),
  });

  const handleSubmit = async (values: RestFormConfigurationType) => {
    const handleResponse = async (response: any) => {
      refresh();
      handleCreateModalClose();
    };

    const getForm = (formId: string) => {
      let url = `${serviceUrl}worker/rest/api/assets/dialob/proxy/forms/${formId}`;
      return session.cFetch(`${url}`,{
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      })
      .then((response:Response)=>handleErrors(response))
      .then((response:Response) => response.json())
      .then ((json:any)=>{
          return json;
      })
      .catch((error:any) => {
        enqueueSnackbar(intl.formatMessage({id: 'dialobForm.downloadFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
      });
    }
    const saveForm = (form: Partial<DialobForm>) => {
      let url = `${serviceUrl}worker/rest/api/assets/dialob/proxy/forms/`;
      return session.cFetch(`${url}`,{
        method: 'POST',
        headers: {
          'Accept': 'application/json'
        },
        body:form
      })
      .then((response:Response)=>handleErrors(response))
      .then((response:Response) => response.json())
      .then ((json:any)=>{
          return json;
      })
      .catch((error:any) => {
        enqueueSnackbar(intl.formatMessage({id: 'dialobForm.saveFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
      });
    }
  
    if (formConfiguration) {    // Copy
      try {
        getForm(formConfiguration.id!)
        .then(json=> {
          delete json._id;
          delete json._rev;
          json.name = values.name!;
          json.metadata.label = values.label || "";
          saveForm(json)
          .then(json=> {
            handleResponse(json);
          });
        })
      } catch (ex:any) {
        enqueueSnackbar(intl.formatMessage({id: 'dialobForm.saveFailed'}, {cause: (ex?.message || 'N/A')}), {variant: 'error'});
      }
    } else {    // Create new
      const result: Partial<DialobForm> = DEFAULT_FORM;
      result.name = values.name!;
      result.metadata!.label = values.label || "";
      try {
        saveForm(result)
        .then(json=> {
          handleResponse(json);
        });
      } catch (ex:any) {
        enqueueSnackbar(intl.formatMessage({id: 'dialobForm.saveFailed'}, {cause: (ex?.message || 'N/A')}), {variant: 'error'});
      }
    }
  };

  return (
    <Box>
      <Dialog
        open={createModalOpen}
        onClose={handleCreateModalClose}
        maxWidth={'lg'}
        sx={{ height: "50%", top: 70 }}
      >
        <DialogTitle sx={{ m: 0, p: "20px 40px" }}>
          <TableHeader id={formConfiguration ? 'dialobForm.heading.copyDialog' : 'dialobForm.heading.addDialog'} />
        </DialogTitle>
        <Divider />
        <DialogContent>
          <Formik
            initialValues={{
              name: undefined,
              label: formConfiguration ? "Copy of " + formConfiguration.metadata.label : "New form",
            }}
            onSubmit={(values) => {
              handleSubmit(values)
            }}
            validationSchema={tagFormSchema}
          >
              {({ isSubmitting, dirty, isValid, touched, errors, submitForm, values, setFieldValue }) => (
                <Form>
                  <Box display="flex" flexDirection="column" p="10px 24px 20px 24px">
                      <Box display="flex" flexDirection="column">
                        <Typography sx={{margin: "8px 0"}}><FormattedMessage id="dialobForm.dialog.formName" /></Typography>
                        <TextField
                          name='name'
                          error={errors.name ? true : false}
                          required
                          onChange={e => setFieldValue('name', e.target.value)}
                          value={values.name}
                          sx={{minWidth: "500px"}}
                        />
                        {errors.name && <FormHelperText error={errors.name ? true : false}>{errors.name}</FormHelperText>} 
                        <Typography sx={{margin: "8px 0"}}><FormattedMessage id="dialobForm.dialog.formLabel" /></Typography>
                        <TextField
                          name='label'
                          onChange={e => setFieldValue('label', e.target.value)}
                          value={values.label}
                          sx={{minWidth: "500px"}}
                        />
                      </Box>
                      <Box display="flex" mt={2} justifyContent="space-between">
                        <Button variant='contained' onClick={handleCreateModalClose} color='secondary'><FormattedMessage id={'button.cancel'} /></Button>
                        <Button variant='contained' onClick={submitForm} disabled={!dirty || (isSubmitting || !isValid)} color='primary'><FormattedMessage id={'button.accept'} /></Button>
                      </Box>
                  </Box>
                </Form>
              )}
          </Formik>
        </DialogContent>
      </Dialog>
    </Box>
  )
}
