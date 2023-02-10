export type DialobDataType = ( 
  DialobDataNote |
  DialobDataBoolean |
  DialobDataDate |
  DialobDataTime |
  DialobDataText |
  DialobDataRoot |
  DialobDataGroup |
  DialobDataRowGroup |
  DialobDataList
);

export interface DialobData { 
  id: string; 
  activeWhen?: string;
}

export interface DialobDataList extends DialobData {
  type: 'list';
  valueSetId: string;
}


export interface DialobDataRoot extends DialobData {
  id: 'questionnaire';
  type: 'questionnaire';
  items: string[]; // ref to another DialobData
}

export interface DialobDataRowGroup extends DialobData {
  type: 'rowgroup';
  label?: DialobDataLabel;
  items: string[] // ref to another DialobData
}

export interface DialobDataGroup extends DialobData {
  type: 'group';
  label?: DialobDataLabel;
  items: string[] // ref to another DialobData
  props?: {} //styling
}

export interface DialobDataTime extends DialobData {
  type: 'time';
  label: DialobDataLabel
}
export interface DialobDataText extends DialobData {
  type: 'text';
  label: DialobDataLabel
  view: 'text' | 'textBox' | string
}
export interface DialobDataBoolean extends DialobData {
  type: 'boolean';
  label: DialobDataLabel
}
export interface DialobDataDate extends DialobData {
  type: 'date';
  label: DialobDataLabel
}
export interface DialobDataNote extends DialobData {
  type: 'note';
  label: DialobDataLabel
}

export type DialobDataLabelLocale = string; // two letter locale
export type DialobDataLabelTranslation = string; // text in locale
export type DialobDataLabel = Record<DialobDataLabelLocale, DialobDataLabelTranslation>;