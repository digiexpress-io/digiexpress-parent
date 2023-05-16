import * as Mathlib from "./mathlib";

test("Mathlib.add", () => {
    const result = Mathlib.add(1, 1);
    expect(result).toBe(2);
});

test("Mathlib.square", () => {
    const result = Mathlib.square(5);
    expect(result).toBe(25);
});

test("Mathlib.squareRoot", () => {
    const result = Mathlib.squareRoot(36);
    expect(result).toBe(6);
});
