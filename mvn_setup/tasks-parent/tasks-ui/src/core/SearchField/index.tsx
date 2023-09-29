import { InputAdornment, TextField, TextFieldProps, styled } from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';

const StyledTextField = styled(TextField)<TextFieldProps>(({ theme }) => ({
  paddingTop: theme.spacing(1), 
  "&>div": {
    paddingBottom: theme.spacing(1), 
    paddingLeft: theme.spacing(2)
  }
}));

const SearchField: React.FC<{ onChange: (value: string) => void }> = ({ onChange }) => {
  return (
    <StyledTextField
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon color='primary' />
          </InputAdornment>
        )
      }}
      fullWidth
      variant='standard'
      placeholder='Search'
      onChange={(e) => onChange(e.target.value)}
    />
  );
}

export default SearchField;