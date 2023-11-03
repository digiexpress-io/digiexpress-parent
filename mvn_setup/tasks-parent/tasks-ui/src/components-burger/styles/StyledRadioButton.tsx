import { Radio } from '@mui/material';
import { styled } from "@mui/material/styles";

const StyledRadioButton = styled(Radio)(({ theme }) => ({
    marginLeft: theme.spacing(1.5),
    color: theme.palette.uiElements.main,
    '&.Mui-checked': {
        color: theme.palette.uiElements.main,
    }
}))

export { StyledRadioButton }