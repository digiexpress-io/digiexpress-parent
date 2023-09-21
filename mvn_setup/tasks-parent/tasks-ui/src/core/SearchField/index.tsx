import { InputAdornment, SxProps, TextField } from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';

const SearchField: React.FC<{ onChange: (value: string) => void, searchFieldSx?: SxProps }> = ({ onChange, searchFieldSx }) => {
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
      sx={searchFieldSx}
    />
  );
}

export default SearchField;