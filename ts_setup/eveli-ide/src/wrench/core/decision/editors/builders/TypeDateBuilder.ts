import {HdesApi} from '../../../client';


type Operator = { key: string, value: string, text: string }

const operators: Operator[] = [
  { key: 'equals', value: 'equals', text: 'equals' },
  { key: 'before', value: 'before', text: 'before' },
  { key: 'after', value: 'after', text: 'after' },
  { key: 'between', value: 'between', text: 'between' },
];


function formatDateFragment(value: number) {
  if (value < 10) {
    return '0' + value;
  }
  return value;
}

function toISODate(value: string, time: boolean) {
  if (!value) {
    return '';
  }
  return time ? value + ':00Z' : value;
}

function isValidDate(value: string, type: HdesApi.TypeDef) {
  if (!value) {
    return false;
  }
  if (type.valueType === "DATE_TIME") {
    return value.length === 20;
  }
  return value.length === 10;
}

function validate(builder: DateBuilder) {
  if (builder.header.direction === 'OUT') {
    return isValidDate(builder.value, builder.header);
  }
  let operator = builder.getOperator();
  if (!isValidOperator(operator)) {
    return false;
  }
  if (operator === 'between') {
    return isValidDate(builder.getStart(true), builder.header) && isValidDate(builder.getEnd(true), builder.header);
  }
  return isValidDate(builder.getStart(true), builder.header);
}

function isValidOperator(possibleOperator: string) {
  let operator: boolean | undefined = undefined;
  for (let index in operators) {
    operator = operators[index].key === possibleOperator;
    if (operator) {
      break;
    }
  }
  return operator;
}

class DateBuilder {
  private _value: string;
  private _time: boolean;
  private _type: HdesApi.TypeDef;
  private _valid: boolean;

  constructor(props: { header: HdesApi.TypeDef, value: string }) {
    this._value = props.value;
    this._time = props.header.valueType === "DATE_TIME";
    this._type = props.header;
    this._valid = !this._value || this._value === '' || validate(this);
  }
  get valid(): boolean {
    return this._valid;
  }
  get header() {
    return this._type;
  }
  get value() {
    return this._value;
  }

  withValue(value: string) {
    return new DateBuilder({ value, header: this._type });
  }

  getOperator() {
    if (!this._value) {
      return '';
    }

    if (this._value.startsWith('between')) {
      return 'between';
    }
    let split = this._value.split(' ');
    return isValidOperator(split[0]) ? split[0] : '';
  }

  getOperators() {
    return operators;
  }

  getValue() {
    if (!this._valid) {
      return this._value;
    }
    let operator = this.getOperator();
    let result = operator + ' ' + this.formatDate(this.getStart());
    if (operator === 'between') {
      return result + ' and ' + this.formatDate(this.getEnd());
    }
    return result;
  }

  getStart(unformatted?: boolean) {
    if (!this._value) {
      return '';
    }
    let operator = this.getOperator();
    let result = this._value;
    if (operator === 'between') {
      result = result.split('and')[0];
    }
    let date = result.substring(operator.length).trim();
    return unformatted ? date : this.getISODateString(date);
  }

  getEnd(unformatted?: boolean) {
    if (!this._value) {
      return '';
    }
    let operator = this.getOperator();
    if (operator !== 'between') {
      return '';
    }

    let result = this._value.split('and')[1].trim();
    if (result === '') {
      return '';
    }
    let date = result.trim();
    return unformatted ? date : this.getISODateString(date);
  }

  withStart(start: string) {
    let operator = this.getOperator();
    let result = operator + ' ' + this.getISODate(start);
    if (operator === 'between') {
      result += ' and ' + this.getISODate(this.getEnd());
    }
    return result;
  }

  withEnd(end: string) {
    let operator = this.getOperator();
    let result = operator + ' ' + this.getISODate(this.getStart());
    if (operator === 'between') {
      result += ' and ' + this.getISODate(end);
    }
    return result;
  }

  getISODate(value: string) {
    return toISODate(value, this._time);
  }
  getISODateString(value: string) {
    if (!value || value.trim() === '') {
      return '';
    }
    try {
      let datetime = new Date(value.trim()).toISOString();
      return this._time ? datetime.substring(0, 16) : datetime.substring(0, 10);
    } catch (e) {
      return '';
    }
  }

  formatDate(value: string) {
    if (!value || value.trim() === '') {
      return '';
    }
    let datetime = new Date(value.trim());
    let month = formatDateFragment(datetime.getMonth() + 1);
    let day = formatDateFragment(datetime.getDate());

    let date = day + '/' + month + '/' + datetime.getFullYear();
    if (this._time) {
      return date + ', ' + formatDateFragment(datetime.getHours()) + ':' + formatDateFragment(datetime.getMinutes());
    }
    return date;
  }

  withOperator(operator: string) {
    let result = operator + ' ' + this.getISODate(this.getStart());
    if (operator === 'between') {
      let end = this.getEnd();
      result += ' and ' + (end ? this.getISODate(end) : '');
    }
    return result;
  }
}


export default DateBuilder;