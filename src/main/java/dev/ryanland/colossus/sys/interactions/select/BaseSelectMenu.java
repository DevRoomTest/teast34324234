package dev.ryanland.colossus.sys.interactions.select;

import dev.ryanland.colossus.Colossus;
import dev.ryanland.colossus.command.executor.functional_interface.CommandConsumer;
import dev.ryanland.colossus.command.executor.functional_interface.CommandPredicate;
import dev.ryanland.colossus.events.repliable.SelectMenuEvent;
import dev.ryanland.colossus.sys.interactions.ComponentRow;
import dev.ryanland.colossus.sys.presetbuilder.PresetBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.Collections;
import java.util.List;

@Getter
public class BaseSelectMenu extends ComponentRow {

    private final SelectMenu selectMenu;
    private final CommandConsumer<SelectMenuEvent> onSubmit;

    /**
     * A Colossus Select Menu. Also see the static methods within this record for helper constructors.
     * @param selectMenu The JDA {@link SelectMenu} object
     * @param onSubmit What to do when this select menu is submitted, with the event given
     */
    public BaseSelectMenu(SelectMenu selectMenu, CommandConsumer<SelectMenuEvent> onSubmit) {
        this.selectMenu = selectMenu;
        this.onSubmit = onSubmit;
    }

    /**
     * A Colossus Select Menu without predefined functionality. See {@link #BaseSelectMenu(SelectMenu, CommandConsumer)} for a version with functionality.<br>
     * Also see the static methods within this record for helper constructors.
     * @param selectMenu The JDA {@link SelectMenu} object
     * @see #BaseSelectMenu(SelectMenu, CommandConsumer)
     */
    public BaseSelectMenu(SelectMenu selectMenu) {
        this(selectMenu, event -> {});
    }

    /**
     * Create a select menu which only works if the provided predicate is true, and do something if false
     */
    public static BaseSelectMenu predicate(CommandPredicate<SelectMenuEvent> predicate,
                                           CommandConsumer<SelectMenuEvent> ifFalse, SelectMenu selectMenu,
                                           CommandConsumer<SelectMenuEvent> onSubmit) {
        return new BaseSelectMenu(selectMenu, event -> {
            if (!predicate.test(event)) ifFalse.accept(event);
            else if (onSubmit != null) onSubmit.accept(event);
        });
    }

    /**
     * Create a select menu which only one user can use
     */
    public static BaseSelectMenu user(Long userId, SelectMenu selectMenu,
                                      CommandConsumer<SelectMenuEvent> onSubmit) {
        return group(new Long[]{ userId }, selectMenu, onSubmit);
    }

    /**
     * Create a select menu which only a specific group of users can use
     */
    public static BaseSelectMenu group(Long[] userIds, SelectMenu selectMenu,
                                       CommandConsumer<SelectMenuEvent> onSubmit) {
        return predicate(event -> List.of(userIds).contains(event.getUser().getIdLong()),
            event -> event.reply(new PresetBuilder(Colossus.getErrorPresetType())
                .setTitle("Not Allowed")
                .setDescription("You're not allowed to use this button.")
            ), selectMenu, onSubmit);
    }

    @Override
    public ActionRow toActionRow() {
        return ActionRow.of(getSelectMenu());
    }

    @Override
    public void startListening(Message message) {
        SelectMenuEvent.addListener(
            message.getIdLong(), this,
            () -> message.editMessageComponents(Collections.emptyList()).queue(success -> {}, error -> {})
        );
    }
}
