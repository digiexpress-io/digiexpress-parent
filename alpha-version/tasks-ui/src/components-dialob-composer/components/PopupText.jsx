import React, {useState} from 'react';
import {Popup, Input, Icon, Label, TextArea} from 'semantic-ui-react';

const PopupText = ({value, onChange}) => {
  const [open, setOpen] = useState(false);

  return (
    <Popup on='click' basic flowing
      open={open}
      onOpen={() => setOpen(true)}
      trigger={
        <Input icon transparent fluid value={value} onChange={(e) => onChange(e.target.value)}>
          <input />
          <Icon name='edit' />
        </Input>
      }>
        <Popup.Header>
          <Label as='a' ribbon='right' color='blue' onClick={() => setOpen(false)}>
            <Icon name='close' />
          </Label>
        </Popup.Header>
        <Popup.Content>
          <TextArea style={{minWidth: '500px'}} value={value} onChange={(e) => onChange(e.target.value)} />
        </Popup.Content>
    </Popup>
  );
}

export {
  PopupText as default
}
