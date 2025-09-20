package net.dividebytoaster.ToastersBundleKeybindFixMod.mixins;

/// Imports - Minecraft
import net.dividebytoaster.ToastersBundleKeybindFixMod.ModMain;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

/// Imports - Mixins - Injections
import org.spongepowered.asm.mixin.*;

@Mixin(BundleItem.class)
public abstract class BundleItemMixIn
{
    @Shadow
    private static void playRemoveOneSound(Entity entity)
    {}

    @Shadow
    private static void playInsertSound(Entity entity)
    {}

    @Shadow
    private static void playInsertFailSound(Entity entity)
    {}

    @Shadow
    protected abstract void onContentChanged(PlayerEntity user);

    /**
     * Cursor clicks slot while holding bundle.
     * @author DivideByToaster
     * @reason Switch left-click and right-click actions.
     */
    @Overwrite
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player)
    {
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);

        // Bundle contents were obtained and cursor right-clicked
        if (bundleContentsComponent != null)
        {
            BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);

            ClickType clickAction = ModMain.key_swap() ? ClickType.RIGHT : ClickType.LEFT;
            boolean   isEmpty     = slot.getStack().isEmpty();

            // Hovered item slot is empty
            if (clickType == ClickType.RIGHT && isEmpty)
            {
                ItemStack itemStack = builder.removeSelected();

                // If there were no items in the bundle, move it to the empty slot
                if (itemStack == null)
                {
                    if (ModMain.on_empty())
                    {
                        BundleItem.setSelectedStackIndex(stack, -1);
                        return false;
                    }
                }

                // Item was removed, so move to hovered slot
                else
                {
                    itemStack = slot.insertStack(itemStack);

                    // TODO - I am unsure what happens here...
                    if (itemStack.getCount() > 0)
                    {
                        builder.add(itemStack);
                    }

                    // TODO - Surely the item stack isn't empty if it was removed?
                    else
                    {
                        playRemoveOneSound(player);
                    }
                }
            }

            // Hovered item is present
            else if (clickType == clickAction && !isEmpty)
            {
                // Insert item if bundle has room
                if (builder.add(slot, player) > 0)
                {
                    playInsertSound(player);
                }

                // Fail to insert item to full bundle
                else
                {
                    playInsertFailSound(player);
                }
            }

            // Invalid click
            else
            {
                return false;
            }

            // Update the bundle's contents
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
            onContentChanged(player);
            return true;
        }
        return false;
    }

    /**
     * Cursor clicks slot containing bundle.
     * @author DivideByToaster
     * @reason Switch left-click and right-click actions.
     */
    @Overwrite
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference)
    {
        boolean isEmpty = otherStack.isEmpty();

        // Empty cursor picks up bundle on left-click
        if (clickType == ClickType.LEFT && isEmpty)
        {
            BundleItem.setSelectedStackIndex(stack, -1);
        }

        // Non-empty cursor (`otherItem`) or right-click
        else
        {
            BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);

            // Bundle contents were obtained
            if (bundleContentsComponent != null)
            {
                BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);

                ClickType clickAction = ModMain.key_swap() ? ClickType.RIGHT : ClickType.LEFT;

                // Empty cursor right-click moves empty bundle or topmost item to cursor
                if (clickType == ClickType.RIGHT && isEmpty)
                {
                    // TODO - I think this checks that the cursor can take an item, but unsure...
                    if (slot.canTakePartial(player))
                    {
                        ItemStack itemStack = builder.removeSelected();

                        // If there are no items in the bundle, move it to the cursor
                        if (itemStack == null)
                        {
                            if (ModMain.on_empty())
                            {
                                BundleItem.setSelectedStackIndex(stack, -1);
                                return false;
                            }
                        }

                        // Otherwise, remove an item
                        else
                        {
                            playRemoveOneSound(player);
                            cursorStackReference.set(itemStack);
                        }
                    }
                }

                // Non-empty cursor
                else if (clickType == clickAction)
                {
                    // Non-empty cursor inserts item if bundle has room
                    if (slot.canTakePartial(player) && builder.add(otherStack) > 0)
                    {
                        playInsertSound(player);
                    }

                    // Non-empty cursor fails to insert item to full bundle
                    else
                    {
                        playInsertFailSound(player);
                    }
                }

                // Invalid click
                else
                {
                    BundleItem.setSelectedStackIndex(stack, -1);
                    return false;
                }

                // Update the bundle's contents
                stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
                onContentChanged(player);
                return true;

            }
        }
        return false;
    }
}
