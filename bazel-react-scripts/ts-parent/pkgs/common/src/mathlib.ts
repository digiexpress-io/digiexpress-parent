export function add(a: number, b: number): number {
    return a + b;
}

export function square(a: number): number {
  console.log("calling square");
    return a ** 2;
}

export function squareRoot(a: number): number {
  console.log("calling squareRootXX");
    return Math.sqrt(a);
}
