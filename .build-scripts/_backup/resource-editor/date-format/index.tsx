import React from 'react';
import moment from 'moment-timezone';


const DATE_FORMAT = "DD.MM.YYYY HH:mm:ss";


export default function DateFormat(props: { children: Date | number[] | number | string }) {
  if(typeof props.children === "string") {
    return (<>
      {moment(props.children).format(DATE_FORMAT)}
    </>);  
  }
  
  if(props.children instanceof Date) {
    return (<>
      {moment(props.children).format(DATE_FORMAT)}
    </>);  
  }
  
  const date = props.children as number[];
  return (<React.Fragment>{`${date[0]}-${date[1]}M-${date[2]}`}</React.Fragment>);
}
