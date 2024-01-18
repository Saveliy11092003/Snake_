package nsu.ccfit.ru.trushkov;

import javafx.stage.Stage;

import lombok.extern.slf4j.Slf4j;

import nsu.ccfit.ru.trushkov.game.controller.impl.*;

import nsu.ccfit.ru.trushkov.game.gui.imp.*;
import nsu.ccfit.ru.trushkov.network.NetworkController;

import java.io.*;
import java.net.InetAddress;

@Slf4j
public class ApplicationMain extends javafx.application.Application {
    private static int port;

    private static String ip;

    private static final GameControllerImpl gameController = new GameControllerImpl();

    public static void main(String[] args) {
        try {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            log.error("usage arg1<port>, arg2<ip>, cannot parse {}, {}", args[0], args[1], e);
            return;
        }
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        log.info("start application");
        NetworkController networkController = new NetworkController(InetAddress.getByName(ip), port, gameController);

        log.info("create view conf menu");
        GUIGameMenuImpl guiGameMenu = new GUIGameMenuImpl(gameController, stage);

        networkController.startReceiverUDP();
        networkController.startMulticastReceiver();
        networkController.startCheckerPlayer();
        networkController.startCheckerMsgACK();

        guiGameMenu.view();
    }
}