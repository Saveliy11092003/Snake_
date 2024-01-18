module nsu.ccfit.ru.trushkov {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    requires org.slf4j;
    requires lombok;
    requires com.google.protobuf;
    requires org.jetbrains.annotations;

    opens nsu.ccfit.ru.trushkov to javafx.fxml;
    exports nsu.ccfit.ru.trushkov;

    opens nsu.ccfit.ru.trushkov.game.controller to javafx.fxml;

    exports nsu.ccfit.ru.trushkov.game.controller;
    exports nsu.ccfit.ru.trushkov.protobuf.snakes;
    exports nsu.ccfit.ru.trushkov.context;
    exports nsu.ccfit.ru.trushkov.observer;
    exports nsu.ccfit.ru.trushkov.observer.context;
    exports nsu.ccfit.ru.trushkov.game.gui;
    exports nsu.ccfit.ru.trushkov.game.model;
    exports nsu.ccfit.ru.trushkov.game.gui.imp;
    exports nsu.ccfit.ru.trushkov.network.model.message;
    exports nsu.ccfit.ru.trushkov.network;
    exports nsu.ccfit.ru.trushkov.ecxeption;
    exports nsu.ccfit.ru.trushkov.game.controller.impl;
    exports  nsu.ccfit.ru.trushkov.network.model.keynode;
    exports nsu.ccfit.ru.trushkov.network.model.thread;
    opens nsu.ccfit.ru.trushkov.game.controller.impl to javafx.fxml;
    exports nsu.ccfit.ru.trushkov.network.model.gamemessage;
}