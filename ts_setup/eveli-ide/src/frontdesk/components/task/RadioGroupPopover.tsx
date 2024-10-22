import React from 'react';
import { FormattedMessage } from 'react-intl';
import { useField, FieldInputProps } from 'formik';
import { Box, Button, FormControl, FormControlLabel, Popover, Radio, RadioGroup, Typography } from '@mui/material';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import { COLORS, ColorMap } from './ColorMap';

interface CommonProps extends FieldInputProps<""> {
  label: string;
  readonly?: boolean;
  messages: Record<string, { id: string; defaultMessage: string }>;
  colorMap: ColorMap;
  handleCallback?: (newValue: string) => void;
}

const getColor = (color: COLORS) => {
  switch (color) {
    case COLORS.YELLOW: return 'brown';
    case COLORS.BLUE: return 'blue';
    case COLORS.GREEN: return 'green';
    case COLORS.GREY: return 'grey';
    case COLORS.RED: return 'red';
    default: return '';
  }
}

const RadioGroupPopover = ({ label, readonly, messages, colorMap, handleCallback, ...props }: CommonProps) => {
  const [field, , helpers] = useField(props);
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [radioValue, setRadioValue] = React.useState<string | undefined>();

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
    setRadioValue(undefined);
  };

  const open = Boolean(anchorEl);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRadioValue(event.target.value);
  };

  const entries = Object.entries(colorMap);

  const handleConfirm = () => {
    if (radioValue) {
      helpers.setValue(radioValue);
      if(handleCallback){
        handleCallback(radioValue);
      }
    }
    setAnchorEl(null);
  };

  return (
    <>
      <Box display="flex" flexDirection="column">
        <Typography>
          {label}
        </Typography>
        <Button
          variant="outlined"
          size='small'
          sx={{ 
            color: getColor(colorMap[field.value]), 
            borderColor: getColor(colorMap[field.value] || COLORS.BLUE), 
            width: "max-content" , 
            borderRadius: 1, 
            borderWidth: 1,
            padding: "4px 8px",
            textTransform: "uppercase",
            "&:hover": {
                borderWidth: 1,
                borderColor: getColor(colorMap[field.value] || COLORS.BLUE),
            }
          }}
          onClick={handleClick}
          endIcon={<ArrowDropDownIcon />}
          disabled={!!readonly}
        >
          {field.value && <FormattedMessage {...messages[field.value]} />}
        </Button>
      </Box>
      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center',
        }}
      >
        <FormControl sx={{ px: 1, pt: 1, pb: 2 }}>
          <RadioGroup value={radioValue || field.value} onChange={handleChange}>
            {entries.map(([value, color]) =>
              <FormControlLabel
                key={value}
                value={value}
                control={<Radio />}
                label={<FormattedMessage {...messages[value]} />}
                sx={{ color: getColor(color), textTransform: "uppercase" }}
              />
            )}
          </RadioGroup>
          <Box display="flex" justifyContent="center">
            <Button color='primary' variant="contained" onClick={handleConfirm} disabled={!radioValue}>
              <FormattedMessage id="button.accept" />
            </Button>
          </Box>
        </FormControl>
      </Popover>
    </>
  );
}

export default RadioGroupPopover;