package client;

import client.controller.Client;
import client.utils.CommandArgs;
import com.beust.jcommander.JCommander;


public class Main {

    public static void main(String[] args) {
        CommandArgs parameters = new CommandArgs();
        JCommander.newBuilder()
                .addObject(parameters)
                .build()
                .parse(args);

        Client.start(parameters);
    }
}
