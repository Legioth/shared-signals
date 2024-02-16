import { Button } from "@hilla/react-components/Button.js";
import { HorizontalLayout } from "@hilla/react-components/HorizontalLayout.js";
import { computed, useSignal } from "@preact/signals-react";
import { FakeGeneratedService } from "../lib/generated/FakeGeneratedService";

const valueSignal = FakeGeneratedService.value({delay: true});
const valueLength = computed(() => valueSignal.value.length);
const listSignal = FakeGeneratedService.list();

// Can pass straight to JSON.stringify since Signal has a toJSON method
const listJson = computed(() => JSON.stringify(listSignal));
const listLength = computed(() => listSignal.value.length);

export default function HelloWorldView() {
  const x = useSignal("");
  return (
    <>
      <HorizontalLayout theme="spacing">
        <Button onClick={() => valueSignal.value += '!'}>Update</Button>
        <Button onClick={event => valueSignal.set(valueSignal.value + "!", false)}>Update value (pessimistic)</Button>
        <Button onClick={event => valueSignal.compareAndSet(valueSignal.value, valueSignal.value + "?")}>CAS</Button>
        <Button onClick={event => valueSignal.update(value => value + "?")}>CAS loop</Button>
        <Button onClick={event => valueSignal.value = ""}>Reset value</Button>
      </HorizontalLayout>
      <div>Value: {valueSignal} <br/>Length: {valueLength}</div>

      <HorizontalLayout theme="spacing">
        <Button onClick={event => listSignal.insertLast("L")}>Insert last</Button>
        <Button onClick={event => listSignal.insertFirst("F")}>Insert first</Button>
        <Button disabled={listSignal.value.length < 2} onClick={event => listSignal.insertBefore(listSignal.value[1], "B") }>Insert before second</Button>
        <Button disabled={listSignal.value.length == 0} onClick={event => listSignal.insertAfter(listSignal.value[0], "A") }>Insert after first</Button>
        <Button disabled={listSignal.value.length == 0} onClick={event => listSignal.value[0].value = "+"}>Update first</Button>
        <Button disabled={listSignal.value.length == 0} onClick={event => listSignal.remove(listSignal.value[0]) }>Remove first</Button>
      </HorizontalLayout>
      <ol>
        { listSignal.value.map(({key, value}) => <li key={key}>{value}</li>) }
      </ol>
      <div>Length: {listLength}</div>
    </>
  )
}
