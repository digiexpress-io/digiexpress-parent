import { InputAdornment, TextField } from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';

const SearchField: React.FC<{ onChange: (value: string) => void }> = ({ onChange }) => {
  return (
    <TextField
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon color='primary' />
          </InputAdornment>
        ),
      }}
      fullWidth
      variant='standard'
      placeholder='Search'
      onChange={(e) => onChange(e.target.value)}
    />
  );
}

export default SearchField;