package Controller;

import Message.*;
import Model.*;
import View.*;

import java.util.*;
import java.util.concurrent.*;

public class Controller {
    private BlockingQueue<Message> queue;
    private List<Valve> valves = new LinkedList<>();
    private View view;
    private Model model;
    private GameInfo gameInfo;

    /**
     * Constructor: Connect the Model, View, and message Queue
     *
     * @param view
     * @param model
     */
    public Controller(View view, Model model, BlockingQueue<Message> queue) {
        this.view = view;
        this.model = model;
        this.queue = queue;
        model.start();
        addAllValves();
    }

    /**
     * Main Loop to Execute the message from the View
     *
     * @throws Exception
     */
    public void mainLoop() throws Exception {
        ValveResponse response = ValveResponse.EXECUTED;
        Message message = null;

        while (response != ValveResponse.FINISH) {
            try {
                message = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Valve valve : valves) {
                response = valve.execute(message);
                if (response != ValveResponse.MISS) break;
            }
        }
    }

    /**
     * @return the View of the App
     */
    public View getView() {
        return view;
    }

    /**
     * @return the model of the App
     */
    public Model getModel() {
        return model;
    }

    /**
     * Update the new Model
     *
     * @return an updated Model wih changes
     */
    private GameInfo updateGameInfo() {
        gameInfo = new GameInfo(model);
        return gameInfo;
    }

    /**
     * Add all the Valve so the mainloop can execute the message
     */
    private void addAllValves() {
        valves.add(new StartNewGameValve());
        valves.add(new MovePieceValve());
        valves.add(new ShowHighlightValve());
    }

    /**
     * Update all the View Panel
     *
     * @param action: Current action of
     */
    private void updateGame(String action) {
        view.setBoardPanel(updateGameInfo(), action);
        view.setHistoryPanel(updateGameInfo(), action);
    }

    /**
     * Start a new Game
     */
    private class StartNewGameValve implements Valve {
        public ValveResponse execute(Message message) {
            if (message.getClass() != NewGameMessage.class) {
                return ValveResponse.MISS;
            }

            // Init the model
            model.start();

            // Update the View of the Game
            updateGame("NEW_GAME");

            return ValveResponse.EXECUTED;
        }
    }

    private class MovePieceValve implements Valve {
        public ValveResponse execute(Message message) {
            if (message.getClass() != MoveMessage.class) {
                return ValveResponse.MISS;
            }

            // Model
            model.movePiece();

            updateGame("MOVE");

            return ValveResponse.EXECUTED;
        }
    }

    private class ShowHighlightValve implements Valve {
        public ValveResponse execute(Message message) {
            if (message.getClass() != ShowHighlightMessage.class) {
                System.out.println("Controller set highlight failed");
                return ValveResponse.MISS;
            }
            System.out.println("Controller set highlight");

            // Model
            model.showHighlight();

            updateGame("HIGHLIGHT");

            return ValveResponse.EXECUTED;
        }
    }


}