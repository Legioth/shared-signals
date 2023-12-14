import { MouseEvent, memo } from "react";
import { Signal, computed, useSignal } from "@preact/signals-react";

import { VerticalLayout } from "@hilla/react-components/VerticalLayout.js";
import { Button } from "@hilla/react-components/Button.js";

import { ListSignal, ValueSignal } from "../lib/SharedSignals";
import { FakeGeneratedService, Position } from "../lib/generated/FakeGeneratedService";
import Counter from "./Counter";

const counter: ValueSignal<number> = FakeGeneratedService.counter();

export default function CounterView() {
    return (
        <VerticalLayout>
            <Counter />
            <span>Confirmed value: {counter}</span>
            <Button onClick={() => counter.value = 0}>Reset</Button>
        </VerticalLayout>
    )
};

