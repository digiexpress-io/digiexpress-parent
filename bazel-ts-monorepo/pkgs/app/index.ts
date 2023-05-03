import { pythagoras } from "./src/functions";

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

main();
