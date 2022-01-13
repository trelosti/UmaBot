package minesweeper;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Game extends ListenerAdapter {
    HashMap<String, Minefield> games = new HashMap<String, Minefield>();

    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() && event.getMessage().getContentRaw().equals("Unknown command, please try again")) {
            event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
        } else {
            String message = "";
            try {
                message = event.getMessage().getContentRaw();

                outer:
                if (games.containsKey(event.getAuthor().getName()) && event.getChannel().getIdLong() == games.get(event.getAuthor().getName()).getChannelId()) {
                    if (games.get(event.getAuthor().getName()).isActive()) {
                        if (!event.getAuthor().isBot()) {
                            String[] selection;
                            String mouseButton;
                            int selX, selY;
                            selection = event.getMessage().getContentRaw().toLowerCase().split(" ");

                            if (selection.length == 1) {
                                switch (selection[0]) {
                                    case "stop":
                                        games.get(event.getAuthor().getName()).disableField();
                                        event.getChannel().sendMessage("The game is stopped. To start a new game type !minesweeper").queue();
                                        break outer;
                                    case "cheat":
                                        games.get(event.getAuthor().getName()).restartShutdownCounter();
                                        games.get(event.getAuthor().getName()).exposeField();
                                        event.getChannel().sendMessage("The state of the field at this point of time. The minefield is mined after the first left click (only for educational purposes)").queue();
                                        break outer;
                                    case "help":
                                        games.get(event.getAuthor().getName()).restartShutdownCounter();
                                        event.getChannel().sendMessage("How to play:\n" +
                                                "The cell in the top left corner has the coordinates of 1 1 (starting point)\n" +
                                                "<e> <x-coord> <y-coord> - press a cell. For example: e 3 4\n" +
                                                "<f> <x-coord> <y-coord> - mark a cell\n" +
                                                "<stop> - stop the current game\n" +
                                                "<cheat> - open all the cells\n" +
                                                "<help> - write instructions for the game").queue();
                                        break outer;
                                    default:
                                        event.getChannel().sendMessage("Unknown command, please try again").queue();
                                        games.get(event.getAuthor().getName()).incrementShutdownCounter();
                                        if (games.get(event.getAuthor().getName()).getShutdownCounter() == 3) {
                                            games.get(event.getAuthor().getName()).disableField();
                                            event.getChannel().sendMessage("The game is stopped due to an excessive amount of unknown commands").queue();
                                        }
                                        break outer;
                                }
                            } else if (selection.length == 3 && (selection[0].equals("e") || selection[0].equals("f"))) {
                                games.get(event.getAuthor().getName()).restartShutdownCounter();
                                mouseButton = selection[0];
                                try {
                                    selX = (Integer.parseInt(selection[1]) - 1);
                                    selY = (Integer.parseInt(selection[2]) - 1);
                                } catch (NumberFormatException exception) {
                                    exception.printStackTrace();
                                    event.getChannel().sendMessage("Write coordinates as numbers in a range between 1 and 8 (inclusive)").queue();
                                    break outer;
                                }
                            } else {
                                games.get(event.getAuthor().getName()).incrementShutdownCounter();
                                if (games.get(event.getAuthor().getName()).getShutdownCounter() == 3) {
                                    games.get(event.getAuthor().getName()).disableField();
                                    event.getChannel().sendMessage("The game is stopped due to an excessive amount of unknown commands").queue();
                                }
                                event.getChannel().sendMessage("Unknown command, please try again").queue();
                                break outer;
                            }

                            /* Mine the field if the first input was a command */
                            if (!games.get(event.getAuthor().getName()).checkIfMined()) {
                                games.get(event.getAuthor().getName()).markAsMined();
                                games.get(event.getAuthor().getName()).mineField(selX, selY);
                            }

                            int cell = games.get(event.getAuthor().getName()).getField()[selY][selX];
                            if (mouseButton.equals("e")) {
                                if (cell == Minefield.MINE_VALUE) {
                                    games.get(event.getAuthor().getName()).unflagCell(selY, selX);
                                    games.get(event.getAuthor().getName()).getUncoveredField()[selY][selX] = true;
                                    games.get(event.getAuthor().getName()).getCells()[selY][selX].setOpened(true);
                                    System.out.println("you lost");
                                    event.getChannel().sendMessage("You lost").queue();
                                    games.get(event.getAuthor().getName()).exposeField();
                                    games.get(event.getAuthor().getName()).disableField();
                                    break outer;
                                } else if (cell == Minefield.EMPTY_VALUE) {
                                    games.get(event.getAuthor().getName()).floodUncover(selX, selY);
                                } else {
                                    if (!games.get(event.getAuthor().getName()).getUncoveredField()[selY][selX]) {
                                        games.get(event.getAuthor().getName()).decrementCellsToUncover();
                            /* check if fails
                            //minefield.getUncoveredField()[selY][selX] = true;
                            */
                                        games.get(event.getAuthor().getName()).getCells()[selY][selX].setOpened(true);
                                        games.get(event.getAuthor().getName()).unflagCell(selY, selX);
                                    }
                                }

                                if (games.get(event.getAuthor().getName()).getCellsToUncover() == 0) {
                                    games.get(event.getAuthor().getName()).getCells()[selY][selX].setOpened(true);
                                    games.get(event.getAuthor().getName()).unflagCell(selY, selX);
                                    games.get(event.getAuthor().getName()).exposeField();
                                    event.getChannel().sendMessage("You won!").queue();
                                    games.get(event.getAuthor().getName()).decrementNumberOfFlags();
                                    games.get(event.getAuthor().getName()).disableField();
                                    break outer;
                                }
                            } else if (mouseButton.equals("f")) {
                                /* Ensure that the number of flags is less than the number of mines */
                                if (games.get(event.getAuthor().getName()).getFlags() < games.get(event.getAuthor().getName()).getMines()) {
                                    /* Check if flagging is legal */
                                    if (!games.get(event.getAuthor().getName()).getUncoveredField()[selY][selX] && !games.get(event.getAuthor().getName()).getFlaggedField()[selY][selX]) {
                                        games.get(event.getAuthor().getName()).flagCell(selY, selX);
                                    } else if (!games.get(event.getAuthor().getName()).getUncoveredField()[selY][selX] && games.get(event.getAuthor().getName()).getFlaggedField()[selY][selX]) {
                                        games.get(event.getAuthor().getName()).unflagCell(selY, selX);
                                    }
                                } else {
                                    if (!games.get(event.getAuthor().getName()).getUncoveredField()[selY][selX] && games.get(event.getAuthor().getName()).getFlaggedField()[selY][selX]) {
                                        games.get(event.getAuthor().getName()).unflagCell(selY, selX);
                                    }
                                }
                            }

                            games.get(event.getAuthor().getName()).present();
                        }
                    }
                }

            } catch (ArrayIndexOutOfBoundsException ex) {
                event.getChannel().sendMessage("Write a command in the following format: <mouse button> x-coord y-coord").queue();
                ex.printStackTrace();
            } catch (Exception ex) {
                event.getChannel().sendMessage("Something went wrong").queue();
                ex.printStackTrace();
            }

            if (message.equals("!minesweeper")) {
                games.put(event.getAuthor().getName(), new Minefield(event, event.getChannel().getIdLong()));
                games.get(event.getAuthor().getName()).present();
                event.getChannel().sendMessage("Welcome to Minesweeper, " + event.getAuthor().getName() + "\n" +
                        "How to play:\n" +
                        "The cell in the top left corner has the coordinates of (1, 1) (starting point)\n" +
                        "<e> <x-coord> <y-coord> - press a cell. For example: e 3 4\n" +
                        "<f> <x-coord> <y-coord> - mark a cell\n" +
                        "<stop> - stop the current game\n" +
                        "<cheat> - open all the cells\n" +
                        "<help> - write instructions for the game").queue();
            }
        }
    }
}
