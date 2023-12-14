import React from 'react'

import { TextareaAutosize, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import Burger from 'components-burger';


interface InputJSONProps {
  value: string;

  onClose: () => void;
  onSelect: (json: object) => void;
}

const parseInput = (value: string) => {
  var parsed = JSON.parse(value);
  for (var key in parsed) {
    if (parsed[key].includes(" - ")) {
      parsed[key] = parsed[key].split(" - ")[0];
    }
    if (parsed[key].includes(", ")) {
      parsed[key] = parsed[key].split(", ")[0];
    }
  }
  var stringified = JSON.stringify(parsed, null, 2);
  return stringified;
}

const InputJSON: React.FC<InputJSONProps> = ({ onSelect, onClose, value }) => {
  const [json, setJson] = React.useState<string>(parseInput(value));
  
  return (<Burger.Dialog title="debug.input.json" open={true} onClose={onClose}
    backgroundColor="uiElements.main"
    submit={{
      title: "buttons.apply",
      disabled: false,
      onClick: () => {
        try {
          onSelect(JSON.parse(json));
          onClose();
        } catch (e) {
          console.error(e);
        }
      }
    }}>

    <Box>
      <Box>
        <Box><Typography variant="h4" fontWeight="bold"><FormattedMessage id={"debug.input.jsonTitle"} /></Typography></Box>
      </Box>

      <TextareaAutosize minRows={10}
        style={{ width: '100%', height: '100%' }}
        value={json}
        onChange={({ target }) => setJson(target.value)}
      />

    </Box>
  </Burger.Dialog>);
}

export type { InputJSONProps };
export { InputJSON };
