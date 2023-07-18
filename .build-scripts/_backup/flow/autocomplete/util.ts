


const parseTemplate = (toBeReplaced: any, template: string[]) => {
  const result: string[] = [];
  for(let v of template) {
    let line: string = v;
    
    for(let key of Object.keys(toBeReplaced)) {
      const replacable = '{' + key + '}';
      if(line.indexOf(replacable) < 0) {
        continue;
      }
      if(toBeReplaced[key] === undefined) {
        return result;
      }
      line = line.replace(replacable, toBeReplaced[key])
    }
    result.push(line)
  }
  return result
}

function toLowerCamelCase(value: string) {
  if(value) {
    return value.replace(/^([A-Z])|\s(\w)/g, function(_match, p1, p2, _offset) {
      if (p2) return p2.toUpperCase();
      return p1.toLowerCase();        
    });    
  }
}

export {parseTemplate, toLowerCamelCase};