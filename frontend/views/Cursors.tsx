import { MouseEvent, memo } from "react";
import { Signal, computed, useSignal } from "@preact/signals-react";

import { VerticalLayout } from "@hilla/react-components/VerticalLayout.js";
import { Button } from "@hilla/react-components/Button.js";

import { ListSignal, ValueSignal } from "../lib/SharedSignals";
import { FakeGeneratedService, Position } from "../lib/generated/FakeGeneratedService";

const cursors: ListSignal<Position> = FakeGeneratedService.cursors();

function getRelativePosition(event: MouseEvent): Position {
    const rect = event.currentTarget.getBoundingClientRect();
    return [event.clientX - rect.left, event.clientY - rect.top];
}

const cursor = "data:;base64,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";

export default function Cursors() {
    const ownCursor = useSignal<null | ValueSignal<Position>>(null);

    function move(event: MouseEvent): void {
        if (event.currentTarget.tagName != "DIV") return;

        const position = getRelativePosition(event);

        if (ownCursor.value) {
            const ownCursorSignal = ownCursor.value;
            ownCursorSignal.value = position;
        } else {
            ownCursor.value = cursors.insertLast(position).signal;
        }
    }

    function leave(event: MouseEvent): void {
        if (ownCursor.value) {
            cursors.remove(ownCursor.value);
            ownCursor.value = null
        }
    }

    return (
        <VerticalLayout>
            <div style={{ position: "relative", width: 400, height: 400, border: "1px solid black" }}
                    onMouseMove={move} onMouseLeave={leave}>
                {
                    cursors.value
                        .filter((signal) => signal != ownCursor.value)
                        .map((signal) => <Cursor key={signal.key} position={signal} />)
                }
            </div>
            <Button onClick={() => cursors.value.forEach((cursor) => cursors.remove(cursor))}>Clear</Button>
            Number of cursors: {cursors.value.length}
        </VerticalLayout>
    )
}
const Cursor = memo(function Cursor({position}: {position: Signal<Position>}) {
    const [left, top] = position.value;

    return <img src={cursor} style={{ position: "absolute", left, top }} />;
});
