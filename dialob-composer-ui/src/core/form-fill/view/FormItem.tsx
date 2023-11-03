import React from 'react';
import { ItemAction } from '@dialob/fill-api';
import { useFillItem } from '@dialob/fill-react';
import {
  Page, Group, Text, Number, TextBox, BooleanCheckbox, BooleanRadio,
  Choice, MultiChoiceAC, MultiChoice, DateField, RowGroup, Row, SurveyGroup
} from '@dialob/fill-material';

import { TimeField } from './TimeField';
import { FillMarkdown } from './FillMarkdown';
import { TextFileUpload } from './TextFileUpload';



const NoteField: React.FC<{ note: ItemAction<'note'>['item'] }> = ({ note }) => {
  const text: string = note.label ? note.label : "";
  return <FillMarkdown text={text} />
}


export interface ItemProps {
  id: string;
  forcePage?: boolean;
  onAttachments: (files: FileList) => Promise<void>;
};
export const FormItem: React.FC<ItemProps> = ({ id, forcePage, onAttachments }) => {
  const { item, errors, availableItems } = useFillItem(id);
  if (!item) return null;

  const withChildren = () => availableItems.map(itemId => <FormItem key={itemId} id={itemId} onAttachments={onAttachments} />)

  if (item.type === 'group' && (item.view === 'page' || forcePage === true)) {
    return <Page page={item} {...{children: withChildren()}} />;
  } else if (item.type === 'group') {
    return <Group group={item} {...{children: withChildren()}}/>;
  } else if (item.type === 'text' && item.view === 'textBox') {
    return <TextBox text={item} errors={errors} />;
  } else if (item.type === 'text' && item.props?.controlType && item.props?.controlType === 'fileUpload') {
    return <TextFileUpload text={item} errors={errors} onAttachments={onAttachments} />;

  } else if (item.type === 'text') {
    return <Text text={item} errors={errors} />;
  } else if (item.type === 'boolean') {
    if (item.props) {
      const display = item.props.display;
      if (display === 'checkbox') {
        return <BooleanCheckbox boolean={item} errors={errors} />;
      }
    }
    return <BooleanRadio boolean={item} errors={errors} />;
  } else if (item.type === 'number' || item.type === 'decimal') {
    return <Number number={item} errors={errors} integer={true}/>;
  } else if (item.type === 'list') {
    return <Choice choice={item} errors={errors} />;
  } else if (item.type === 'multichoice' && (item.props?.autocomplete === 'true' || item.props?.autocomplete === true)) {
    return <MultiChoiceAC multichoice={item} errors={errors} />
  } else if (item.type === 'multichoice') {
    return <MultiChoice multichoice={item} errors={errors} />;
  } else if (item.type === 'note') {

    return <NoteField note={item} />;

  } else if (item.type === 'date') {
    return <DateField datefield={item} errors={errors} />
  } else if (item.type === 'time') {

    return <TimeField timefield={item} errors={errors} />

  } else if (item.type === 'rowgroup') {
    return <RowGroup rowGroup={item} {...{children: withChildren()}}/>;
  } else if (item.type === 'row') {
    return <Row row={item}>{id => (<FormItem id={id} key={id} onAttachments={onAttachments} />)}</Row>;
  } else if (item.type === 'surveygroup') {
    return <SurveyGroup surveyGroup={item} {...{children: withChildren()}}/>;
  } else if (item.type === 'survey') {
    /*
    Surveys should not be handled through Item, as they depend on the encompassing
    SurveyGroup and should never be rendered as separate entities
    */
    return null;
  } else if (item.type === 'questionnaire') {
    throw new Error('Questionnaire should be handled outside Item');
  } else {
    const itemProxy: any = item;
    //throw new Error(`Unknown item type '${itemProxy.type}'!`);
    console.log(`Unsupported item type '${itemProxy.type}'`);
    return null;
  }
}