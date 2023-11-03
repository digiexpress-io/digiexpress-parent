import { InputAdornment, TextField } from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';


const SearchFieldBar: React.FC<{ onChange: (value: string) => void }> = ({ onChange }) => {
  return (
    <TextField
      InputProps={{
        sx: {
          py: 0,
          px: 3,
          borderRadius: 10,
          width: '50ch',
          height: '2rem',
          '&.MuiOutlinedInput-root': {
            backgroundColor: 'mainContent.main',
            '&.Mui-focused fieldset': {
              borderColor: 'uiElements.main',
              borderWidth: '1px'
            }
          }
        },
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon sx={{ fontSize: '20px', color: 'uiElements.main' }} />
          </InputAdornment>
        )
      }}
      variant='outlined'
      placeholder='Search'
      onChange={(e) => onChange(e.target.value)}
    />
  );
}

const SearchFieldPopover: React.FC<{ onChange: (value: string) => void }> = ({ onChange }) => {
  return (
    <TextField
      InputProps={{
        sx: { py: 1, pl: 2 },
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



export { SearchFieldPopover, SearchFieldBar };