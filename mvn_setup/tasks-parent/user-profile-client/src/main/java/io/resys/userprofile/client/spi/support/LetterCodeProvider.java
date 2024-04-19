package io.resys.userprofile.client.spi.support;

import java.util.List;

public class LetterCodeProvider {
  private final List<String> reserved;
  private final NameIndex nameIndex;
  
  private LetterCodeProvider(List<String> reserved, String firstName, String lastName) {
    super();
    this.reserved = reserved;
    this.nameIndex = new NameIndex(firstName, lastName);
  }
 
  public String getNextCode() {
    String nextCode;
    do {
      nextCode = nameIndex.next(); 
    } while(reserved.contains(nextCode));
    
    return nextCode;
  }


  private static class NameIndex {
    private final String first;
    private final String last;
    private int index_first;
    private int index_last;
    private int index_fallback;
    
    public NameIndex(String first, String last) {
      super();
      this.index_first = 0;
      this.index_last = 0;
      this.index_fallback = 0;
      
      if(first.isBlank()) {
        this.first = "X";
      } else {
        this.first = first;
      }
      
      if(last.isBlank()) {
        this.last = "Y";
      } else {
        this.last = last;
      }
    }
    
    private String getCodeA() {
      if(index_first < first.length()) {
        return first.charAt(index_first) + "";
      }
      return first.charAt(0) + "" + index_fallback;
    }
    private String getCodeB() {
      if(index_last < last.length()) {
        return last.charAt(index_last) + "";
      }
      return last.charAt(0) + "";
    }
    
    public String next() {
      final var result = getCodeA() + getCodeB();
      if(index_last < last.length()) {
        index_last++;
      } else if(index_first < first.length()) {
        index_first++;
        index_last = 0;
      } else {
        index_fallback++;
      }
      return result.toUpperCase();
    }
  }
  public static LetterCodeProvider getInstance(List<String> reserved, String firstName, String lastName) {
    return new LetterCodeProvider(reserved, firstName, lastName);
  } 
}
