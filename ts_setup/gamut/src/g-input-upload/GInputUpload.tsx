import React from 'react'

import { OverridableStringUnion } from '@mui/types'
import { CircularProgress, IconButton, Table, TableBody, TableCell, TableHead, TableRow, TextField, Tooltip, useThemeProps } from '@mui/material'

import AddIcon from '@mui/icons-material/Add';
import ClearIcon from '@mui/icons-material/Clear';

import { DialobApi, useDialob, useForm } from '../api-dialob'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'
import { GInputBase, GInputBaseAnyProps, GInputBaseProps } from '../g-input-base'

import { MUI_NAME, GInputUploadRoot, useUtilityClasses } from './useUtilityClasses'
import { FormattedMessage } from 'react-intl';


// extension hook for adding custom input types
export interface GInputUploadPropsVariantOverrides { }

export interface GInputUploadProps {
  id: string;
  value: string | undefined; // list of file names
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;
  disabled: boolean;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required?: boolean | undefined;


  variant: OverridableStringUnion<
    'upload',
    GInputUploadPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'upload',
    GInputUploadPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputUploadProps>;
}


export const GInputUpload: React.FC<GInputUploadProps> = (initProps) => {
  
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { variant = 'upload', labelPosition, errors } = props;
  const classes = useUtilityClasses(props.id, variant);
  const ownerState = { ...props, variant };

  const { id, label, description } = props;
  const slots: GInputBaseProps<GInputUploadProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: UploadInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, ...props },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: description, title: label ?? '', disabled: props.disabled }
    }
  }

  return (<GInputUploadRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputUploadRoot>);
}


const UploadInput: React.FC<GInputBaseAnyProps & GInputUploadProps> = (props) => {  

  const { fetchAttachmentPost } = useDialob();
  const { executionId } = useForm();
  
  const inputFile = React.useRef<HTMLInputElement>(null);
  
  const dialobInput = React.useRef<HTMLInputElement>(null);
  const [dialobInputValue, setDialobInputValue] = React.useState<string>(props.value ? props.value + '' : '[]');
  const [sync, setSync] = React.useState<boolean>(false);

  const [loading, setLoading] = React.useState(false);
  const fileNames: string[] = props.value ? JSON.parse(props.value) : [];

  const { onChange } = props;

  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    dialobInput.current?.addEventListener("input", poulateTheChange);
    return () => dialobInput.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  React.useEffect(() => {
    if(sync) {
      const event = new Event('input', { bubbles: true });
      dialobInput.current?.dispatchEvent(event);
    }
  },[sync, dialobInputValue]);


  function handleFileUpload(event: React.SyntheticEvent<HTMLInputElement>) {
    setLoading(true);

    const files = event.currentTarget.files;
    if (!files || files.length === 0) {
      return;
    }
    const names: string[] = [...fileNames];
    for (const file of Array.from(files)) {
      if (!names.includes(file.name)) {
        names.push(file.name);
      }
    }
    
    fetchAttachmentPost(executionId, files)
      .then(() => {
        setDialobInputValue(JSON.stringify(names));
        setSync(true);
      })
      .then(() => setLoading(false));
  }

  function handleFileDelete(index: number) {
    const names: string[] = [...fileNames];
    names.splice(index, 1)
    setDialobInputValue(JSON.stringify(names));
    setSync(true);
  }
  function doNothing() {

  }

  return (
    <>
    <input hidden value={dialobInputValue} ref={dialobInput} onChange={doNothing} />
    <Table>
      <TableHead>
        <TableRow>
          <TableCell align="right" colSpan={2}>
              <input type='file' id='file' multiple ref={inputFile} style={{ display: 'none' }} onChange={handleFileUpload} accept='.jpg, .jpeg, .png, .pdf' />
              <IconButton disabled={props.disabled} onClick={() => inputFile.current?.click()}>
              <AddIcon color="primary" />
            </IconButton>
          </TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {fileNames.map((name, index) => (
          <TableRow key={index}>
            <TableCell>
              <TextField
                fullWidth
                sx={{ pointerEvents: 'none' }}
                inputProps={{ readOnly: true }}
                label={<FormattedMessage id="attachment.fileName" values={{ index: index + 1 }} />}
                value={name}
                error={(props.errors?.length ?? 0) > 0}
              />
            </TableCell>
            <TableCell align="right">
              <Tooltip title={<FormattedMessage id='attachment.remove' />}>
                <IconButton onClick={() => handleFileDelete(index)}>
                  <ClearIcon />
                </IconButton>
              </Tooltip>
            </TableCell>
          </TableRow>
        ))
        }
        {loading ? (<TableRow><TableCell colSpan={2}><CircularProgress color="inherit" /></TableCell></TableRow>) : null}
      </TableBody>
    </Table>
    </>)
}