import React from 'react';
import numbro from 'numbro';

function initNumber(value: string | undefined, format: numbro.Format) {
  if(value === undefined) {
    return undefined;
  }
  return numbro.unformat(value, format);
}


export const InputHidden: React.FC<{ 
  id: string;
  value: string | undefined;
  format: numbro.Format;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {


  const {id, value, format, onChange} = props;
  const newInputValue = initNumber(value, format);
  const [inputValue, setInputValue] = React.useState(initNumber(value, format))
  const ref = React.useRef<HTMLInputElement>(null); 


  React.useEffect(() => {

    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      console.log("trigger", {id, newInputValue});
      
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, value]);

  // trigger event on the hidden input that will contain technical date
  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  return (<input name={id} hidden value={newInputValue ?? ''} ref={ref} onChange={() => {}} />);
}