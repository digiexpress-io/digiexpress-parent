import { Mathlib } from "@bazelmono/common";

export function pythagoras(a: number, b: number): number {
    return Mathlib.squareRoot(
        Mathlib.add(Mathlib.square(a), Mathlib.square(b))
    );
}
