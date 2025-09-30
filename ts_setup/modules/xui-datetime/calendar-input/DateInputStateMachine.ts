export type DateInputState = 'empty' | 'typing' | 'validating' | 'valid' | 'error';
export type DateField = 'day' | 'month' | 'year';

export interface DateInputData {
  day: string;
  month: string;
  year: string;
  state: DateInputState;
  focusedField: DateField | null;
  error?: string;
  shouldAutoAdvance?: boolean;
  resultDate?: Date;
  isUserChange: boolean;
}

export class DateInputStateMachine {
  data: DateInputData;

  constructor(initialData?: Partial<DateInputData>) {
    this.data = {
      day: '',
      month: '',
      year: '',
      state: 'empty',
      focusedField: null,
      isUserChange: false,
      ...initialData
    };
  }

  // Public getters for React to consume
  get day() { return this.data.day; }
  get month() { return this.data.month; }
  get year() { return this.data.year; }
  get state() { return this.data.state; }
  get focusedField() { return this.data.focusedField; }
  get error() { return this.data.error; }
  get shouldAutoAdvance() { return this.data.shouldAutoAdvance; }
  get resultDate() { return this.data.resultDate; }
  get isUserChange() { return this.data.isUserChange; }
  get isValid() { return this.data.state === 'valid'; }

  // State transitions
  public typeDayDigit(value: string): DateInputStateMachine {
    const cleanValue = value.replace(/\D/g, '').slice(0, 2);
    const previousLength = this.data.day.length;
    
    const newData = {
      ...this.data,
      day: cleanValue,
      state: 'typing' as DateInputState,
      focusedField: 'day' as DateField,
      shouldAutoAdvance: false,
      error: undefined,
      isUserChange: true
    };

    // Auto-advance logic
    if (cleanValue.length === 2 && cleanValue.length > previousLength) {
      const dayNum = parseInt(cleanValue);
      if (dayNum >= 10 && dayNum <= 31) {
        newData.shouldAutoAdvance = true;
        newData.focusedField = 'month';
      }
    }

    const machine = new DateInputStateMachine(newData);
    return machine.tryValidateComplete();
  }

  public typeMonthDigit(value: string): DateInputStateMachine {
    const cleanValue = value.replace(/\D/g, '').slice(0, 2);
    const previousLength = this.data.month.length;
    
    const newData = {
      ...this.data,
      month: cleanValue,
      state: 'typing' as DateInputState,
      focusedField: 'month' as DateField,
      shouldAutoAdvance: false,
      error: undefined,
      isUserChange: true
    };

    // Auto-advance logic
    if (cleanValue.length === 2 && cleanValue.length > previousLength) {
      const monthNum = parseInt(cleanValue);
      if (monthNum >= 1 && monthNum <= 12) {
        newData.shouldAutoAdvance = true;
        newData.focusedField = 'year';
      }
    }

    const machine = new DateInputStateMachine(newData);
    return machine.tryValidateComplete();
  }

  public typeYearDigit(value: string): DateInputStateMachine {
    const cleanValue = value.replace(/\D/g, '').slice(0, 4);
    
    const newData = {
      ...this.data,
      year: cleanValue,
      state: 'typing' as DateInputState,
      focusedField: 'year' as DateField,
      shouldAutoAdvance: false,
      error: undefined,
      isUserChange: true
    };

    const machine = new DateInputStateMachine(newData);
    return machine.tryValidateComplete();
  }

  public focusField(field: DateField): DateInputStateMachine {
    return new DateInputStateMachine({
      ...this.data,
      focusedField: field
    });
  }

  public blurField(field: DateField): DateInputStateMachine {
    const newData = { ...this.data };
    
    // Apply zero padding on blur
    if (field === 'day' && this.data.day.length === 1) {
      newData.day = this.data.day.padStart(2, '0');
    }
    if (field === 'month' && this.data.month.length === 1) {
      newData.month = this.data.month.padStart(2, '0');
            console.log('next value', newData.month)
    }
    
    // Clear focus if leaving the component entirely
    newData.focusedField = null;
    
    const machine = new DateInputStateMachine(newData);
    return machine.tryValidateComplete();
  }

  public clear(): DateInputStateMachine {
    return new DateInputStateMachine({
      day: '',
      month: '',
      year: '',
      state: 'empty',
      focusedField: 'day',
      error: undefined,
      shouldAutoAdvance: false,
      resultDate: undefined
    });
  }

  public setFromDate(date: Date | null): DateInputStateMachine {
    if (!date) {
      return new DateInputStateMachine({
        day: '',
        month: '',
        year: '',
        state: 'empty',
        focusedField: null,
        error: undefined,
        shouldAutoAdvance: false,
        resultDate: undefined,
        isUserChange: false  // UNLOAD the user change flag
      });
    }

    if( date.getMonth() === this.data.resultDate?.getMonth() &&
        date.getDay() === this.data.resultDate?.getDay() &&
        date.getFullYear() === this.data.resultDate?.getFullYear()
    ) {
      return this;
    }

    return new DateInputStateMachine({
      ...this.data,
      day: date.getDate().toString().padStart(2, '0'),
      month: (date.getMonth() + 1).toString().padStart(2, '0'),
      year: date.getFullYear().toString(),
      state: 'valid',
      resultDate: date,
      error: undefined,
      isUserChange: false  // UNLOAD the user change flag
    });
  }

  // Private methods
  private isComplete(): boolean {
    return this.data.day.length > 0 && 
           this.data.month.length > 0 && 
           this.data.year.length > 0;
  }

  private tryValidateComplete(): DateInputStateMachine {
    if (!this.isComplete()) {
      return this;
    }

    const newData = { ...this.data };
    newData.state = 'validating';

    const dayNum = parseInt(this.data.day);
    const monthNum = parseInt(this.data.month);
    const yearNum = parseInt(this.data.year);

    // Basic range validation
    if (dayNum < 1 || dayNum > 31 || 
        monthNum < 1 || monthNum > 12 || 
      yearNum <= 1925 || yearNum >= 2100) {
      newData.state = 'error';
      newData.error = 'Invalid date range';
      return new DateInputStateMachine(newData);
    }

    // Create date and validate it's real
    const date = new Date(yearNum, monthNum - 1, dayNum);
    const isValidDate = date.getFullYear() === yearNum &&
                       date.getMonth() === monthNum - 1 &&
                       date.getDate() === dayNum;

    if (isValidDate) {
      newData.state = 'valid';
      newData.resultDate = date;
      newData.error = undefined;
    } else {
      newData.state = 'error';
      newData.error = 'Invalid date';
    }

    return new DateInputStateMachine(newData);
  }

  // For debugging - because we'll need it
  public debug(): string {
    return `DateInputStateMachine: ${this.data.state} | ${this.data.day}/${this.data.month}/${this.data.year} | focus: ${this.data.focusedField} | error: ${this.data.error}`;
  }
}