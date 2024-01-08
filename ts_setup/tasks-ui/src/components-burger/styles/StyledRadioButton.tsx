import { Radio } from '@mui/material';
import { styled } from "@mui/material/styles";
import { cyan } from 'components-colors';

const StyledRadioButton = styled(Radio)(({ theme }) => ({
    marginLeft: theme.spacing(1.5),
    color: cyan,
    '&.Mui-checked': { color: cyan }
}))

export { StyledRadioButton }