import React from 'react';
import { Button, Typography} from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { blue_mud } from 'components-colors';


interface ButtonSearchProps {
  id: string,
  onClick: (event: React.MouseEvent<HTMLElement>) => void,
  values: {} | undefined
}

const ButtonSearch: React.FC<ButtonSearchProps> = ({ onClick, id, values }) => {
  return (
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: blue_mud }} onClick={onClick}>
      <Typography variant='caption' sx={{ color: 'text.primary', fontWeight: 'bolder' }}>
        <FormattedMessage id={id} values={values} />
      </Typography>
    </Button>
  )
}
export type { ButtonSearchProps }
export { ButtonSearch };