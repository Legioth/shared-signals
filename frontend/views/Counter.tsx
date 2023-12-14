import { Button } from "@hilla/react-components/Button.js";
import { FakeGeneratedService as CounterService } from "../lib/generated/FakeGeneratedService";

const counter = CounterService.counter();

export default function Counter() {
    return (
        <Button onClick={() => counter.update((value) => value + 1)}>
            Click count: { counter }
        </Button>
    )
};