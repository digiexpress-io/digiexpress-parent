import React from 'react';

import { DateTime } from 'luxon';


function initDate(dateTime: DateTime | null) {
  return dateTime?.isValid ? dateTime?.toFormat('yyyy-MM-dd'): '';
}


export const InputHidden: React.FC<{ 
  id: string;
  dateTime: DateTime | null;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {
  const {id, dateTime, onChange} = props;
  const newInputValue = initDate(dateTime);
  const [inputValue, setInputValue] = React.useState(initDate(dateTime))
  const ref = React.useRef<HTMLInputElement>(null); 


  React.useEffect(() => {

    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      console.log("trigger", {id, newInputValue});
      
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, dateTime]);

  // trigger event on the hidden input that will contain technical date
  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  return (<input name={id} hidden value={newInputValue} ref={ref} onChange={() => {}} />);
}