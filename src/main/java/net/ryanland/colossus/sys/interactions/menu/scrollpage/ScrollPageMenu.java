package net.ryanland.colossus.sys.interactions.menu.scrollpage;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.ryanland.colossus.Colossus;
import net.ryanland.colossus.command.CommandException;
import net.ryanland.colossus.events.repliable.RepliableEvent;
import net.ryanland.colossus.sys.interactions.ComponentRow;
import net.ryanland.colossus.sys.interactions.button.BaseButton;
import net.ryanland.colossus.sys.interactions.button.ButtonLayout;
import net.ryanland.colossus.sys.interactions.button.ButtonRow;
import net.ryanland.colossus.sys.interactions.menu.InteractionMenu;
import net.ryanland.colossus.sys.message.PresetBuilder;

import java.util.ArrayList;
import java.util.List;

public class ScrollPageMenu implements InteractionMenu {

    private record ScrollPage(PresetBuilder message, List<ComponentRow> rows) {}

    private final List<ScrollPage> pages;
    private final int startPage;

    public ScrollPageMenu(List<PresetBuilder> pages, int startPage) {
        this.pages = pages.stream().map(msg -> new ScrollPage(msg, msg.getComponentRows())).toList();
        this.startPage = startPage;
    }

    public ScrollPageMenu(List<PresetBuilder> pages) {
        this(pages, 0);
    }

    @Override
    public void send(RepliableEvent event) throws CommandException {
        event.reply(renderPage(event.getUser().getIdLong(), startPage));
    }

    private PresetBuilder renderPage(long userId, int page) {
        PresetBuilder message = pages.get(page).message();
        message.setComponentRows(new ArrayList<>(pages.get(page).rows));
        message.getComponentRows().add(0, new ButtonRow(
            // previous page
            BaseButton.user(userId, Button.primary("previous", Emoji.fromUnicode("⬅")).withDisabled(page <= 0), event -> {
                event.reply(renderPage(userId, page - 1));
            }),
            // current page
            BaseButton.user(userId, Button.secondary("current", "Page " + (page+1) + "/" + pages.size()).withDisabled(pages.size() <= 1), event -> {
                event.reply(Modal.create("page", "Select Page").addActionRow(TextInput
                    .create("page", "Page", TextInputStyle.SHORT).setPlaceholder("Enter page number...").build()).build(), evt -> {
                    int newPage;
                    try {
                        newPage = Integer.parseInt(evt.getValue("page").getAsString());
                    } catch (NumberFormatException e) {
                        newPage = 0;
                    }
                    if (newPage < 1 || newPage > pages.size()) {
                        evt.reply(new PresetBuilder(Colossus.getErrorPresetType(), "Invalid Page", "Invalid page number provided."));
                        return;
                    }
                    evt.reply(renderPage(userId, newPage - 1));
                });
            }),
            // next page
            BaseButton.user(userId, Button.primary("next", Emoji.fromUnicode("➡")).withDisabled(page + 1 >= pages.size()), event -> {
                event.reply(renderPage(userId, page + 1));
            })));
        return message;
    }
}
