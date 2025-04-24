import { EveliDatePicker } from "@/eveli-datepicker";
import { EveliDateTimeFormatter } from "@/eveli-datetime-formatter";
import { Box, Chip, Grid2, InputLabel, Paper, TextField, Typography } from "@mui/material";
import { FormattedMessage } from "react-intl";
import { TaskFormDelegateProps } from "./TaskFormState";
import { TaskApi } from "@/api-task";

const classes = {
  keywordChip: {
    width: "max-content",
    ml: 1
  },
};


export interface EveliTaskHeaderProps {
  keywords: string[];
  readOnly: boolean;
  createdAt: any;
  form: TaskFormDelegateProps;
}


export const EveliTaskHeader: React.FC<EveliTaskHeaderProps> = (props) => {
  const { keywords, readOnly, form, createdAt } = props;
  const isProtected = keywords.includes('Protected');
  const isManual = keywords.includes('Manual');
  const { errors, currentState, setFieldValue } = form;

  return (
    <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
      <Grid2 container spacing={2} alignItems="center">
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Box display='flex' alignItems='center'>
            <InputLabel><FormattedMessage id='taskDialog.category' />: </InputLabel>
            <Chip
              label={isProtected ? <FormattedMessage id='Protected' /> : <FormattedMessage id='Normal' />}
              color={isProtected ? 'error' : 'primary'}
              sx={classes.keywordChip}
            />
          </Box>
        </Grid2>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Typography>
            <FormattedMessage id={'task.created'} />:&nbsp;<EveliDateTimeFormatter value={createdAt} variant="text"/>
          </Typography>
        </Grid2>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <EveliDatePicker
            label={<FormattedMessage id='taskDialog.dueDate' />}
            fullWidth={true}
            readonly={readOnly}
            value={currentState.dueDate}
            onChange={newDate => setFieldValue('dueDate', newDate)}
          />
        </Grid2>
      </Grid2>
      <Grid2 container spacing={2} alignItems="top" sx={{ mt: 1 }}>
        <Grid2 size={{ xs: 12, md: 6 }}>
          <TextField
            label={<FormattedMessage id='taskDialog.clientIdentificator' />}
            fullWidth={true}
            inputProps={{
              readOnly: readOnly
            }}
            value={currentState.clientIdentificator}
            onChange={event => setFieldValue('clientIdentificator', event.target.value)}
          />
        </Grid2>
        <Grid2 size={{ xs: 12, md: 6 }}>
          <TextField
            label={<FormattedMessage id='taskDialog.subject' />}
            required
            error={!!errors.subject}
            helperText={errors.subject}
            fullWidth={true}
            inputProps={{
              readOnly: readOnly
            }}
            value={currentState.subject}
            onChange={event => setFieldValue('subject', event.target.value)}
          />
        </Grid2>

        <Grid2 size={{ xs: 12, md: 12 }}>
          <TextField
            label={<FormattedMessage id='taskDialog.additionalInfo' />}
            fullWidth={true}
            inputProps={{
              readOnly: readOnly,
              maxLength: 100
            }}
            value={currentState.additionalInfo}
            onChange={event => setFieldValue('additionalInfo', event.target.value)}
          />
        </Grid2>
      </Grid2>
      <Grid2 container spacing={2} alignItems="center" sx={{ mt: 1 }}>
        <Grid2 size={{ xs: 12, md: 12 }}>
          <Box display='flex' alignItems='center'>
            <InputLabel><FormattedMessage id='taskDialog.source' />: </InputLabel>
            <Chip
              label={isManual ? <FormattedMessage id='Internal'/> : <FormattedMessage id='CustomerCreated' />}
              color='primary'
              sx={classes.keywordChip}
            />
          </Box>
        </Grid2>
      </Grid2>
    </Paper>
  );
}
