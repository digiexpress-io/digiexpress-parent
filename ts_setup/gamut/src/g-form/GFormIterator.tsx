import React from 'react';
import { useForm, useFormTip } from '../api-dialob';
import { GFormBase } from '../g-form-base';


export const GFormIterator: React.FC<{}> = (props) => {
  const tip = useFormTip();
  return (<>{tip?.items?.map((item) => <GFormItem key={item}>{item}</GFormItem>)}</>);
}

// render each fill item and/or it's children
const GFormItem: React.FC<{ children: string }> = (props) => {
  const { store } = useForm();
  const stateId = props.children;
  const state = store.form.getItem(stateId);

  // state not available
  if (!state) {
    return (<React.Fragment key={props.children} />);
  }

  // grouping item with children
  if (state.items) {
    return (
      <GFormBase id={stateId}>
        {state.items.map((item) => <GFormItem key={item}>{item}</GFormItem>)}
      </GFormBase>);
  }

  // none grouping item
  return <GFormBase id={stateId} />
}
