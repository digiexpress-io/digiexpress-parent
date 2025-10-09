import React from 'react';
import { Box, Button, Container, MenuItem, FormControl, ListItemText, Select, Checkbox, Chip, Stack, Typography } from '@mui/material';
import { SelectChangeEvent } from '@mui/material/Select';
import LocalPrintshopIcon from '@mui/icons-material/LocalPrintshop';
import { useIntl } from 'react-intl';
import { createFileRoute } from '@tanstack/react-router'
import { DialobReviewBasedOnForm } from '../dialob-review';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/review/')({
  component: Component,
})

function Component() {
  const { taskId } = Route.useParams();
  const backend = useTaskBackend();
  const options = React.useState<TaskApi.TaskPdfRequest['fields']>([]);

  async function handlePdfClick() {
    const pdfBlob = await backend.persistence.getOneTaskPdf({ taskId, fields: options[0] });
    const pdfUrl = URL.createObjectURL(pdfBlob);
    const _newWindow = window.open(pdfUrl, '_blank');
  }

  return (
    <Container>
      {React.useMemo(() => (
        <DialobReviewBasedOnForm
          taskId={taskId}
          questionnaireId={''}
          onClose={() => { }}
        />
      ), [taskId])}

      <Box mt={3} className='GFormPage-root'>
        <PdfOptionsSelect state={options} onPdfCreate={handlePdfClick} />
      </Box>
    </Container>
  )
}


const options: TaskApi.TaskPdfRequest['fields'] = [
  'CUSTOMER_NAME',
  'CUSTOMER_SSN',
  'EXTERNAL_COMMENTS'
];

const PdfOptionsSelect: React.FC<{
  onPdfCreate: () => void,
  state: [TaskApi.TaskPdfRequest['fields'], React.Dispatch<React.SetStateAction<TaskApi.TaskPdfRequest['fields']>>]
}> = ({ onPdfCreate, state }) => {
  const intl = useIntl();
  const [optionValues, setOptionValues] = state;

  const handleChange = (event: SelectChangeEvent<typeof optionValues>) => {
    const value = event.target.value as TaskApi.TaskPdfRequest['fields'];
    setOptionValues(value);
  };

  return (
    <Box display='flex' flexDirection='column' gap={2} p={3} width='100%'>
      <Stack direction='column'>
        <Box display='flex' justifyContent='space-between' alignItems='flex-end'>
          <Stack direction='column'>
            <Typography fontWeight={500}>
              {intl.formatMessage({ id: 'task.pdf.options.select' })}
            </Typography>
            <Typography variant='subtitle2'>
              {intl.formatMessage({ id: 'task.pdf.options.select.desc' })}
            </Typography>
          </Stack>
          <Button variant='contained' onClick={onPdfCreate} startIcon={<LocalPrintshopIcon />}>
            {intl.formatMessage({ id: 'task.pdf.print' })}
          </Button>
        </Box>
        <FormControl fullWidth>
          <Select sx={{ height: '3.5rem' }}
            multiple fullWidth
            value={optionValues}
            onChange={handleChange}
            renderValue={(selected) => {
              return (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {selected.map((value) => (
                    <Chip key={value} label={intl.formatMessage({ id: `task.pdf.options.select.${value}` })} />
                  ))}
                </Box>
              );
            }}
          >
            {options.map((option) => (
              <MenuItem key={option} value={option}>
                <Checkbox checked={optionValues.includes(option)} />
                <ListItemText primary={intl.formatMessage({ id: `task.pdf.options.select.${option}` })}
                />
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Stack>

    </Box>
  );
}