import * as EditorAPI from './EditorAPI';

const optimize = (values1: EditorAPI.ViewCommand[], values2?: EditorAPI.ViewCommand[]): EditorAPI.ViewCommand[] => {
  const all = [...values1, ...(values2 ? values2 : [])];
  const optimized: EditorAPI.ViewCommand[] = [];
  
  for (let index = 0; index < all.length; index++) {
    const command = all[index];
    const nextIndex = index + 1;
    
    if (nextIndex === all.length) {
      optimized.push(command);
      continue;
    }

    // same line changes merged together
    const nextCommand = all[nextIndex];
    if (nextCommand.id === command.id &&
      nextCommand.type === command.type) {
      continue;
    } else {
      optimized.push(command);
    }
  }
  return optimized;
}

export default optimize;