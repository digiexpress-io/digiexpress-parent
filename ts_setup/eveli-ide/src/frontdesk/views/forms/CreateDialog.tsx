import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, TextField, Divider, Typography, FormHelperText, Dialog, Button } from '@mui/material';
import { Form, Formik } from 'formik';
import * as Yup from 'yup';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { DEFAULT_FORM, DialobForm, DialobFormEntry } from '../../types';
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
  const { getForm } = useFetch('worker/rest/api/assets/dialob/proxy/forms/$formId.GET', {})
  const { saveForm } = useFetch('worker/rest/api/assets/dialob/proxy/forms.POST', {})

  const tagFormSchema = () => Yup.object().shape({
    name: Yup.string().required(intl.formatMessage({id: "error.valueRequired"})).matches(/^[_\-a-zA-Z\d]*$/g,intl.formatMessage({id: "dialobForm.error.invalidFormName"})),
  });

  const handleSubmit = async (values: RestFormConfigurationType) => {
    const handleResponse = async (response: any) => {
      refresh();
      handleCreateModalClose();
    }

    if (formConfiguration) {
      // Copy
      getForm(formConfiguration.id!).then(json => {
        delete json._id;
        delete json._rev;
        json.name = values.name!;
        json.metadata.label = values.label || "";
        saveForm(json).then(json => handleResponse(json));
      })

    } else {    
      // Create new
      const result: Partial<DialobForm> = DEFAULT_FORM;
      result.name = values.name!;
      result.metadata!.label = values.label || "";
      saveForm(result).then(json => handleResponse(json));
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
