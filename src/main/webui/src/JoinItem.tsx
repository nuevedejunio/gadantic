import { createUniqueId, ParentProps } from "solid-js";

type Props = { groupName: string };

export default function ({ groupName, children }: ParentProps<Props>) {
  const id = createUniqueId();
  return (
    <div class="join-item
        grayscale
        has-[:checked]:filter-none
        hover:filter-none
        hover:scale-115
        hover:z-99">
      <label for={id}>
        <input type="radio" id={id} name={groupName} class="appearance-none" />
        {children}
      </label>
    </div>
  );
}
