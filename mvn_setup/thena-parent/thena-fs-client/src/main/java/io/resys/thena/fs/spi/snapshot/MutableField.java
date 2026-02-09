package io.resys.thena.fs.spi.snapshot;

public class MutableField<T> {

  private boolean isNewValueSet;
  private T newValue;
  
  public MutableField() {
    this.isNewValueSet = false;
    this.newValue = null;
  }
  
  public boolean isNewValueSet() {
    return isNewValueSet;
  }
  public T getNewValue() {
    return newValue;
  }
  
  public MutableField<T> withNewValue(T newValue) {
    this.newValue = newValue;
    this.isNewValueSet = true;
    return this;
  }
  
  public T orElse(T fallbackValue) {
    if(isNewValueSet) {
      return newValue;
    }
    return fallbackValue;
  }
}
