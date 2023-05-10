import { pythagoras } from "./src/functions";
import App  from "./src/App"
function main(): void {
    const a = 3;
    const b = 4;
    const result = pythagoras(a, b);
    console.log(`
==============================

Pythagoras Theorm:
${result} = √( (${a})^2  + (${b})^2 )

==============================`);

    console.log(pythagoras.toString());
}

//export { App };
main();



