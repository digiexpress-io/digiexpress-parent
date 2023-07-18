import { Hdes } from '../../deps';


type Operator = { key: string, value: string, text: string }
const comparisonOperators: Operator[] = [
  {key: '=', value: '=', text: '='},
  {key: '<', value: '<', text: '<'},
  {key: '<=', value: '<=', text: '<='},
  {key: '>', value: '>', text: '>'},
  {key: '>=', value: '>=', text: '>='}
];


function validate(builder: NumberBuilder) {
  if(builder.header.direction === 'OUT') {
    return isValidNumber(builder.value, builder.header);
  }
  // Comparison operator
  if(builder.getType() === 'operator') {
    if(!isValidOperator(builder.getOperator())) {
      return false;
    }
    return builder.getValue() ? true : false;
  }
  return isValidNumber(builder.getStart(), builder.header) && isValidNumber(builder.getEnd(), builder.header);
}

function isValidOperator(possibleOperator: string) {
  let operator;
  for(let index in comparisonOperators) {
    operator = comparisonOperators[index].key === possibleOperator;
    if(operator) {
      break;
    }
  }
  return operator;
}

function isValidNumber(value: string | null, type: Hdes.AstAPI.DtHeader) {
  if(!value) {
    return false;
  }
  let isInteger = /^\d+$/.test(value);
  let isDouble = ((value as any) % 1 !== 0); 
  if(type.value === 'INTEGER' || type.value === 'LONG') {
    return isInteger && !isDouble;
  } else if(type.value === 'DECIMAL') {
    return isInteger || isDouble;
  }
  return false;
}

class NumberBuilder {
  private _value: string;
  private _type: Hdes.AstAPI.DtHeader; 
  private _valid: boolean;
  private _comparisonOperators: Operator[];
  
  constructor(props: {header: Hdes.AstAPI.DtHeader, value: string}) {
    this._value = props.value;
    this._type = props.header;
    this._comparisonOperators = comparisonOperators;
    this._valid = !props.value || props.value === '' || validate(this);
  }
  
  get comparisonOperators() {
    return this._comparisonOperators;
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
  
  // type - range / comparison
  getType() {
    if(this._value && (this._value.startsWith("(") || this._value.startsWith("[") )) {
      return 'range';
    }
    return 'operator';
  }
  
  getStart() {
    if(this._value) {
      let split = this._value.split('..');
      return split[0].substring(1).trim();
    }
    return null;
  }
  
  isStart() {
    if(this._value) {
      return this._value.startsWith('[');
    }
    return false;
  }
  
  isEnd() {
    if(this._value) {
      return this._value.endsWith(']');
    }
    return false;
  }
  
  getEnd() {
    if(this._value) {
      let split = this._value.split('..');
      return split[1].substring(0, split[1].length - 1).trim();
    }
    return null;
  }
  
  getOperator() {
    if(this._value) {
      let split = this._value.split(' ');
      let operator = split[0].trim();
      return isValidOperator(operator) ? operator : '';
    }
    return '';
  }
  
  getValue() {
    if(this._value) {
      let split = this._value.split(' ');
      if(split.length > 1) {
        return isValidNumber(split[1], this._type) ? split[1] : '';        
      }
      return isValidNumber(this._value, this._type) ? this._value : '';
    }
    return '';
  }
  
  withOperator(operator: string) {
    return operator + ' ' + this.getValue();
  }
  withOperatorValue(value: string) {
    return this.getOperator() + ' ' + value;
  }
  withStart(value: string) {
    return this._value.substring(0, 1) + value + '..' + this.getEnd() + this._value.substring(this._value.length - 1, this._value.length);
  }
  withEnd(value: string) {
    return this._value.substring(0, 1) + this.getStart() + '..' + value + this._value.substring(this._value.length - 1, this._value.length);
  }
  withStartInclude(value: boolean) {
    let include = value ? '[' : '(';
    return include + this._value.substring(1);
  }
  withEndInclude(value: boolean) {
    let include = value ? ']' : ')';
    return this._value.substring(0, this._value.length - 1) + include;
  }
  withType(type: string) {
    return type === 'range' ? '(..)' : '=';
  }
}

export default NumberBuilder;
