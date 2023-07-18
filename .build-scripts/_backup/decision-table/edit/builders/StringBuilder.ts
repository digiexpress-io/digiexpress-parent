import { Hdes } from '../../deps';

type Operator = { key: string, value: string, text: string }
const operators: Operator[] = [
  { key: 'in', value: 'in', text: 'in' },
  { key: 'not in', value: 'not in', text: 'not in' }
];

function validate(builder: StringBuilder) {
  if (builder.header.direction === 'OUT') {
    return true;
  }
  let possibleOperator = builder.getOperator();
  let operator;
  for (let index in operators) {
    operator = operators[index].key === possibleOperator;
    if (operator) {
      break;
    }
  }
  if (!operator) {
    return false;
  }
  return builder.getValues().length > 0;
}

class StringBuilder {
  private _value: string;
  private _type: Hdes.AstAPI.DtHeader;
  private _valid: boolean;
  private _operators: Operator[];

  constructor(props: { header: Hdes.AstAPI.DtHeader, value: string }) {
    this._value = props.value
    this._type = props.header
    this._valid = !props.value || validate(this);
    this._operators = operators;
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
  get operators() {
    return this._operators;
  }

  getOperator() {
    if (!this._value) {
      return '';
    }
    return this._value.split('[')[0].trim();
  }

  withOperator(operator: string) {
    return operator + JSON.stringify(this.getValues());
  }

  withNewValue(value: string) {
    let values = this.getValues();
    if (value !== null && values.filter(v => v === value).length === 0) {
      values.push(value);
    }
    let operator = this.getOperator();
    let operatorValue = JSON.stringify(values);
    if (operator) {
      return operator + operatorValue;
    }
    return operatorValue;
  }

  remove(index: number) {
    let values = this.getValues();
    values.splice(index, 1);
    return this.getOperator() + ' ' + JSON.stringify(values);
  }

  getValues(): string[] {
    if (!this._value) {
      return [];
    }
    let index = this._value.indexOf('[');
    if (index < 0) {
      return [];
    }

    try {
      let result = this._value.substring(index);
      return JSON.parse(result);
    } catch (e) {
      return [];
    }
  }
}

export default StringBuilder;

