import React, { useEffect, useState } from 'react'
import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, TextField, Divider, Typography, FormHelperText, Button, Dialog } from '@mui/material';
import { useAdminBackend } from '../backend';
import { checkHttpResponse, handleRejection } from '../middleware';
import type { DefaultForm, FormConfiguration, DialobAdminConfig } from '../types';
import { DEFAULT_FORM } from '../util';

export interface CreateDialogProps {
  createModalOpen: boolean;
  handleCreateModalClose: () => void;
  formConfiguration?: FormConfiguration;
  setFetchAgain: React.Dispatch<React.SetStateAction<boolean>>;
  config: DialobAdminConfig;
}

export const CreateDialog: React.FC<CreateDialogProps> = ({
  setFetchAgain,
  createModalOpen,
  handleCreateModalClose,
  formConfiguration,
  config
}) => {
  const intl = useIntl();
  const [values, setValues] = useState({
    name: '',
    label: formConfiguration ? "Copy of " + formConfiguration.metadata.label : "New form"
  });
  const [errors, setErrors] = useState<{ name?: string; label?: string }>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { addAdminFormConfiguration, getAdminFormConfiguration } = useAdminBackend(config);

  const validateField = (field: string, value: string): string | undefined => {
    if (field === 'name') {
      if (!value) return intl.formatMessage({ id: 'error.valueRequired' });
      if (!/^[_\-a-zA-Z\d]*$/.test(value)) {
        return intl.formatMessage({ id: 'error.invalidFormName' });
      }
    }
    if (field === 'label') {
      if (!value) return intl.formatMessage({ id: 'error.valueRequired' });
    }
    return undefined;
  };

  const handleChange = (field: keyof typeof values, value: string) => {
    setValues(prev => ({ ...prev, [field]: value }));
    const error = validateField(field, value);
    setErrors(prev => ({ ...prev, [field]: error }));
  };

  const handleSubmit = async () => {
    const nameError = validateField('name', values.name);
    const labelError = validateField('label', values.label);
    if (nameError || labelError) {
      setErrors({ name: nameError, label: labelError });
      return;
    }
    setIsSubmitting(true);

    const handleResponse = async (response: any) => {
      try {
        const jsonResponse = await response.json();
        setFetchAgain((prevState) => !prevState);
        handleCreateModalClose();
        return jsonResponse;
      } catch (ex) {
        handleRejection(ex, config.setTechnicalError);
        throw ex;
      }
    };

    const checkHttpResponseAsync = async (response: any, setLoginRequired: any) => {
      try {
        return await checkHttpResponse(response, setLoginRequired);
      } catch (ex) {
        handleRejection(ex, config.setTechnicalError);
        throw ex;
      }
    };

    if (formConfiguration) {    // Copy
      try {
        const response = await getAdminFormConfiguration(formConfiguration.id!);
        const checkedResponse = await checkHttpResponseAsync(response, config.setLoginRequired);
        const json = await handleResponse(checkedResponse);
        delete json._id;
        delete json._rev;
        json.name = values.name!;
        json.metadata.label = values.label || "";
        const addResponse = await addAdminFormConfiguration(json);
        await checkHttpResponseAsync(addResponse, config.setLoginRequired);
        await handleResponse({ json: () => json });
      } catch (ex) {
        handleRejection(ex, config.setTechnicalError);
      }
    } else {    // Create new
      const result: DefaultForm = DEFAULT_FORM;
      result.name = values.name!;
      result.metadata.label = values.label || "";
      try {
        const addResponse = await addAdminFormConfiguration(result);
        await checkHttpResponseAsync(addResponse, config.setLoginRequired);
        await handleResponse(addResponse);
      } catch (ex) {
        handleRejection(ex, config.setTechnicalError);
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  useEffect(() => {
    if (createModalOpen) {
      setValues({
        name: '',
        label: formConfiguration ? "Copy of " + formConfiguration.metadata.label : "New form"
      });
      setErrors({});
      setIsSubmitting(false);
    }
  }, [createModalOpen, formConfiguration]);

  return (
    <Box>
      <Dialog
        sx={{
          padding: '0 20px 20px 20px',
          border: 'none',
          height: '50%',
          top: "20%"
        }}
        open={createModalOpen}
        onClose={handleCreateModalClose}
        maxWidth={'lg'}
      >
        <DialogTitle sx={{ p: 3 }}>
          <Typography variant="h4" component="div">
            <FormattedMessage id={formConfiguration ? 'heading.copyDialog' : 'heading.addDialog'} />
          </Typography>
        </DialogTitle>
        <Divider />
        <DialogContent sx={{ p: 3 }}>
          <Box sx={{ display: "flex", flexDirection: "column" }}>
            <Box sx={{ display: "flex", flexDirection: "column" }}>
              <Typography sx={{ my: 1, mx: 0 }}><FormattedMessage id="adminUI.dialog.formName" /></Typography>
              <TextField
                name='name'
                error={!!errors.name}
                required
                onChange={e => handleChange('name', e.target.value)}
                value={values.name}
                sx={{ minWidth: "500px" }}
              />
              {errors.name && <FormHelperText error>{errors.name}</FormHelperText>}
              <Typography sx={{ my: 1, mx: 0 }}><FormattedMessage id="adminUI.dialog.formLabel" /></Typography>
              <TextField
                name='label'
                error={!!errors.label}
                required
                onChange={e => handleChange('label', e.target.value)}
                value={values.label}
                sx={{ minWidth: "500px" }}
              />
              {errors.label && <FormHelperText error>{errors.label}</FormHelperText>}
            </Box>
            <Box sx={{ display: "flex", mt: 2, justifyContent: "space-between" }}>
              <Button onClick={handleCreateModalClose}><FormattedMessage id={'button.cancel'} /></Button>
              <Button onClick={handleSubmit} disabled={isSubmitting || !values.name || !!errors.name || !!errors.label} ><FormattedMessage id={'button.accept'} /></Button>
            </Box>
          </Box>
        </DialogContent>
      </Dialog>
    </Box>
  )
}
